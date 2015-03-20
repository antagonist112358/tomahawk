import net.mentalarray.doozie.Builders.WorkflowBuilder
import net.mentalarray.doozie.DBStore._
import net.mentalarray.doozie.DSL._
import net.mentalarray.doozie.Tasks.{HiveTask, ScalaTask, SqoopTask}
import net.mentalarray.doozie._



/**
 * Created by kdivincenzo on 10/28/14.
 */

class DieYieldWorkflow extends WorkflowBuilder("DieYieldWorkflow") {

  private final val rndString: Unit => String = _ => java.util.UUID.randomUUID().toString

  private final val nameOnlyRegex = """\W+(INT|DOUBLE|TIMESTAMP|VARCHAR\(\d*\))""".r

  private val hiveDynamicPartitions = List(
    "SET hive.exec.max.dynamic.partitions.pernode=10000",
    "SET mapred.max.split.size=256000000",
    "SET hive.exec.dynamic.partition.mode=nonstrict",
    "SET hive.exec.dynamic.partition=true",
    "SET hive.enforce.bucketing = true",
    "SET hive.exec.compress.output=true;",
    "SET mapred.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;",
    "SET mapred.output.compression.type=BLOCK;",
    "USE rawdata"
  )


  private final val nowTime = DateTime.now()

  // Set the DB Config for the job
  Configuration.setDBConfiguration("DieYieldWorkflow")

  private val bootStrapDate = "2014-06-01"
  private val batchSizeDays = 3

  private val startTime: DateTime = Configuration[DateTime]("SqoopTask", "startTime", bootStrapDate)
  private val startDate: String = startTime.toString("yyyy-MM-dd")
  private val endDate: String = startTime.toDateMidnight.plusDays(batchSizeDays).toString("yyyy-MM-dd")

  private val name = "DieYield"

  private val staging_name = name + "_staging"
  private val stagingPath = s"/etl/staging/$name/%s"

  private val columnInfo = """
      ROOT_LOT_ID       VARCHAR(8),
      WAFER_ID          INT,
      PROCESS_ID        VARCHAR(40),
      PART_ID           VARCHAR(40),
      STEP_SEQ				  VARCHAR(16),
      EQP_ID					  VARCHAR(30),
      LAST_UPDATE_TIME	TIMESTAMP,
      START_LINE_ID			VARCHAR(8),
      FLAT_ZONE				  INT,
      LAST_FLAG				  VARCHAR(2),
      LOT_TYPE				  VARCHAR(8),
      PGM_ID					  VARCHAR(40),
      PROBE_CARD_ID			VARCHAR(30),
      REWORK_CNT				INT,
      TEST_TYPE				  INT,
      TKIN_TIME				  TIMESTAMP,
      TKOUT_TIME				TIMESTAMP,
      CHIP_X_MAX_POS		INT,
      CHIP_X_MIN_POS		INT,
      CHIP_Y_MAX_POS		INT,
      CHIP_Y_MIN_POS		INT,
      CHIP_X_SIZE				DOUBLE,
      CHIP_Y_SIZE				DOUBLE,
      CHIP_X_POS				INT,
      CHIP_Y_POS				INT,
      BIN_NO					  INT,
      DUT_NO					  INT""".stripMargin

  private val etlColumns = """
       ROOT_LOT_ID     	VARCHAR(8),
       LAST_UPDATE_TIME	TIMESTAMP,
       PART_ID         	VARCHAR(40),
       PROCESS_ID      	VARCHAR(40),
       STEP_SEQ			VARCHAR(16),
       START_LINE_ID		VARCHAR(8),
       EQP_ID				VARCHAR(30),
       FLAT_ZONE			INT,
       LAST_FLAG			VARCHAR(2),
       LOT_TYPE			VARCHAR(8),
       PGM_ID				VARCHAR(40),
       PROBE_CARD_ID		VARCHAR(30),
       REWORK_CNT			INT,
       TEST_TYPE			INT,
       TKIN_TIME			TIMESTAMP,
       TKOUT_TIME			TIMESTAMP,
       WAFER_ID        	INT,
       CHIP_X_MAX_POS		INT,
       CHIP_X_MIN_POS		INT,
       CHIP_Y_MAX_POS		INT,
       CHIP_Y_MIN_POS		INT,
       CHIP_X_SIZE			DOUBLE,
       CHIP_Y_SIZE			DOUBLE,
       BIN_NO				INT,
       CHIP_X_POS			INT,
       CHIP_Y_POS			INT,
       DUT_NO				INT""".stripMargin

  private val columnNamesOnly = nameOnlyRegex.replaceAllIn(columnInfo, "")

  private val extractQuery = """
      SELECT 				
      	TR.ROOT_LOT_ID,
      	TR.LAST_UPDATE_TIME,
      	TR.PART_ID,
      	ST.PROCESS_ID,
      	ST.STEP_SEQ,
      	ST.START_LINE_ID,
      	TR.EQP_ID,
      	TR.FLAT_ZONE,
      	TR.LAST_FLAG,
      	TR.LOT_TYPE,
      	TR.PGM_ID,
      	TR.PROBE_CARD_ID,
      	TR.REWORK_CNT,
      	TR.TEST_TYPE,
      	TR.TKIN_TIME,
      	TR.TKOUT_TIME,
      	CAST(TR.WAFER_ID AS INT) WAFER_ID, 			
      	TR.CHIP_X_MAX_POS,
      	TR.CHIP_X_MIN_POS,
      	TR.CHIP_Y_MAX_POS,
      	TR.CHIP_Y_MIN_POS,
      	TR.CHIP_X_SIZE,
      	TR.CHIP_Y_SIZE,
      	CB.BIN_NO,
      	CB.CHIP_X_POS,
      	CB.CHIP_Y_POS,
      	CB.DUT_NO
      FROM
         MST_STEPSEQ_ALL ST,
         TRK_EDS_WF_TRACKING TR,
         SRT_EDS_CHIP_BIN CB
      WHERE
              TR.LOT_KEY = CB.LOT_KEY
        	AND ST.STEP_KEY = TR.STEP_KEY 			
        	AND TR.ROOT_LOT_ID = CB.ROOT_LOT_ID 			
        	AND TR.WAFER_ID = CB.WAFER_ID 			
        	AND TR.STEP_KEY+0 = CB.STEP_KEY
          AND TR.TKOUT_TIME = CB.TKOUT_TIME
        	AND ST.LINE_ID = 'SFBX'
        	AND TR.TKOUT_TIME >= to_date('$start_time', 'yyyy-MM-dd')
        	AND TR.TKOUT_TIME <  to_date('$end_time', 'yyyy-MM-dd')
          AND $CONDITIONS WITH UR
    """.stripMargin

  private val externalTableHQL = s"""
    create external table etl.$staging_name (
      $etlColumns
    )
    row format delimited fields terminated by ','
    stored as textfile
    location '%s'
    """.stripMargin

  private val dropExternalTableHQL = s"drop table if exists etl.$staging_name"

  private val insertIntoHiveTable = s"""
       insert into table $name partition (process_id_partition, YYYYMM)
       SELECT $columnNamesOnly,
         PROCESS_ID,
         from_unixtime(unix_timestamp(to_date(TKOUT_TIME), 'yyyy-MM-dd'),'yyyyMM')  YYYYMM
         from etl.$staging_name
     """.stripMargin


  /*
   Enough config -- here is the workflow:
    1) Parse out the SqoopTarget directory
    2) Execute Sqoop import job
    3) Drop external table
    4) Create external table
    5) Insert into Hive table
    6) Drop external table
    7) Update configuration
   */

  println(s"Import range is from $startDate (inclusive) to $endDate (exclusive).")

  // 1) Parse out the SqoopTarget directory
  private lazy val actualStagingPath = stagingPath format rndString()
  // ** FOR TESTING ** val actualStagingPath = "/etl/staging/BinYield/255af0c0-e2cb-4da3-979e-cc933e9fc0de"



  // 2) Execute Sqoop import job (skip)
  appendStep(SqoopTask(s"Sqooping DieYield from YMS") { task =>

    task.connect(JDBCConnection.YMS.connectionString)
      .username(DBConnectionString.YMS.ymsuser)
      .password(DBConnectionString.YMS.ymspwd)
      .query(ParameterizedQuery(extractQuery, Replace("start_time" -> startDate, "end_time" -> endDate)))
      .targetDir(actualStagingPath)
  })

  // 3) Drop external table
  appendStep(HiveTask(s"Dropping etl table: $staging_name") { task =>
    task.appendNonQuery(dropExternalTableHQL)
  })

  // 4) Create external table
  appendStep(HiveTask(s"Creating etl table: $staging_name") { task =>
    task.appendNonQuery(externalTableHQL format actualStagingPath)
  })

  // 5) Insert into Hive table
  appendStep(HiveTask(s"Importing RawData into hive table: $name") { task =>
    task.setNonQuery(hiveDynamicPartitions)
    task.appendNonQuery(insertIntoHiveTable)
  })

  // 6) Drop external table
  appendStep(HiveTask(s"Dropping etl table: $staging_name") { task =>
    task.appendNonQuery(dropExternalTableHQL)
  })

  // 7) Update configuration
  appendStep(ScalaTask {
    Configuration("SqoopTask", "startTime") = endDate
    true
  })
}


new DieYieldWorkflow()