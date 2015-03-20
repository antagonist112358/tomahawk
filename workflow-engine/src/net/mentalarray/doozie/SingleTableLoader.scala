package net.mentalarray.doozie

import com.github.nscala_time.time.Imports._

class SingleTableLoader(config: SingleTableConfig) {

  private lazy val workflowName: String = config.workflowName
  private lazy val daysToLoad: Int = config.daysToLoad
  private lazy val bootStrapDate: String = config.bootStrapDate
  private lazy val stagingTablename: String = config.stagingTablename
  private lazy val importTableDB = config.importTableDB
  private lazy val importTableColumns: String = config.getSourceColumnList
  private lazy val sourceTable: String = config.sourceTable
  private lazy val destinationTable: String = config.destinationTable
  private lazy val insertTableSQL: String = config.insertTableSQL
  private lazy val dateField: String = config.dateField
  private lazy val partitionList: String = config.partitionList


  private val startTime: DateTime = Config[DateTime]("SqoopTask", "startTime", bootStrapDate)
  private val startDate: String = startTime.toString("yyyy-MM-dd")
  private val endDate = (startTime + daysToLoad.day).toDateTime.toString("yyyy-MM-dd")
  private val stagingPath = "/etl/staging/%s/%s" format(stagingTablename, "%s")
  private val connectionStr = importTableDB.connectionString
  private val queryStr = """ SELECT * FROM %s """ +
                         """ WHERE %s >= to_date('$START', 'yyyy-MM-dd') AND %s < to_date('$END', 'yyyy-MM-dd') AND $CONDITIONS WITH UR""" format ( sourceTable, dateField, dateField )
  private val queryParams = Replace("START" -> startDate, "END" -> endDate)
  private val hiveDynamicPartitions: List[String] = List("SET hive.exec.max.dynamic.partitions.pernode=10000",
                                                         "SET mapred.max.split.size=256000000",
                                                         "SET hive.exec.dynamic.partition.mode=nonstrict",
                                                         "SET hive.exec.dynamic.partition=true",
                                                         "SET hive.enforce.bucketing = true",
                                                         "SET mapred.output.compression.type=BLOCK",
                                                         "SET mapred.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec",
                                                         "SET hive.exec.compress.output=true",
                                                         "USE rawdata")

  lazy val onImportError: ScalaTask = {
      ScalaTask {
        Config("SqoopTask", "startTime") = startDate
        true
      }
  }

  //TODO: Find all variables to update not just SqoopTask
  lazy val updateConfiguration: ScalaTask = {
    ScalaTask {
        Config("SqoopTask", "startTime") = endDate
        true
    }
  }

  lazy val clearStaging: HdfsTask  = {
    HdfsTask (task => {
      task.setCommand(s"rm -r -f %s" format stagingPath format "*")
      task.ignoreError = true
    })
  }

  lazy val importData: SqoopTask = {
    SqoopTask (task => {
      task.connect(connectionStr)
        .username(importTableDB.user)
        .password(importTableDB.password)
        .query(ParameterizedQuery(queryStr, queryParams))
        .targetDir(stagingPath format startDate)
    }
    )}

  lazy val dropHiveStagingTable : HiveTask = {
    HiveTask ( task => {
      task.setNonQuery(List("drop table if exists etl.%s_staging" format destinationTable ))
    } ) }
  lazy val createExternalTable: HiveTask = {
    HiveTask ( task => {
      task.setNonQuery(List( """ create external table etl.%s_staging ( %s )
                                 row format delimited fields terminated by ','
                                 stored as textfile
                                 location '%s'""" format (destinationTable, importTableColumns, stagingPath format startDate)))
    })
  }

  lazy val insertImportedData: HiveTask = {
    HiveTask ( task => {
      task.setNonQuery(hiveDynamicPartitions ++ List(insertTableSQL format (destinationTable, partitionList, "etl.%s_staging" format destinationTable ) ) )
    })
  }

}

object SingleTableLoader{

  def apply(tableConfig: SingleTableConfig) = {
    new SingleTableLoader(tableConfig)
  }

}

case class SingleTableConfig( workflowName: String,
                              daysToLoad: Int,
                              bootStrapDate: String,
                              stagingTablename: String,
                              importTableDB: SchemaBuilder,
                              sourceTable: String,
                              destinationTable: String,
                              insertTableSQL: String,
                              dateField: String,
                              partitionList: String){

  def getSourceColumnList = {
    importTableDB.schema(sourceTable.replaceFirst("""^\w*\.""","")).foldLeft( List[String]() ){ (res: List[String], a: (String, String)) => res :+ "%s %s".format(a._1,a._2) }.mkString(",")
  }

}

