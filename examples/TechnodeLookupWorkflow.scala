
import net.mentalarray.doozie.Builders.WorkflowBuilder
import net.mentalarray.doozie.DBStore.JDBCConnection
import net.mentalarray.doozie.Logging
import net.mentalarray.doozie.Tasks.ScalaTask

/**
 * Created by bgilcrease on 10/14/14.
 */
class TechnodeLookup extends WorkflowBuilder("TechnodeLookup") with Logging {


  val selectQuery =
    """
      SELECT DISTINCT PROCESS_ID, SUBSTR(STEP_ID,0,1) AS StepKey,
	CASE
		WHEN SUBSTR(STEP_ID,0,1) = 'F' THEN '14nm'
		WHEN SUBSTR(STEP_ID,0,1) = 'J' THEN '20nm'
		WHEN SUBSTR(STEP_ID,0,1) = 'Z' THEN '28nm'
		WHEN SUBSTR(STEP_ID,0,1) = 'H' THEN '32nm'
		WHEN SUBSTR(STEP_ID,0,1) = 'Y' THEN '45nm'
		ELSE 'Other'
	END AS TechNode
 FROM "SOR"."MST_STEPSEQ_ALL"
 WHERE PROCESS_ID like 'U%' and LINE_ID = 'SFBX' and STEP_ID is not null and STEP_ID not like 'B%' and STEP_ID not like 'E%'
 with ur
   """

  val truncateQuery = "truncate table technodelookup"

  appendStep( ScalaTask {
      val results = JDBCConnection.YMS.fetchSeq(selectQuery, None)
      JDBCConnection.HiveMetastore.executeNonQuery(List(truncateQuery))
      JDBCConnection.HiveMetastore.insertBatch(table = "technodelookup", results)
      true
  })
}


new TechnodeLookup()
