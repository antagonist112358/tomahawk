
import net.mentalarray.doozie.Builders.WorkflowBuilder
import net.mentalarray.doozie.DSL.TextFile
import net.mentalarray.doozie.Logging
import net.mentalarray.doozie.Tasks.{HdfsTask, PigTask, SqoopTask}

/**
 * Created by bgilcrease on 10/10/14.
 */

class PartIDWorkflow extends WorkflowBuilder("PartIDWorkflow") with Logging {

  appendStep(HdfsTask { task =>
    task.setCommand("rm -r -f /etl/staging/PartID/")
  })

  appendStep(SqoopTask { task =>
    task.connect(DBConnectionString.YMS.connectionString)
      .username(DBConnectionString.YMS.user)
      .password(DBConnectionString.YMS.password)
      .query("""SELECT DISTINCT part.PART_ID,
                            SUBSTR (part.PART_ID, 0, 8) AS PARTID8,
                            part.DEVICE,
                            part.SUB_DEVICE,
                            part.FAB_LINE_ID,
                            part.FAB_PROCESS_ID,
                            part.PRCGROUP,
                            part.PROC_DESC
                FROM sor.mst_part part
                WHERE    FAB_LINE_ID LIKE 'KFBK'
                OR     FAB_LINE_ID LIKE 'SFBX'
                AND $CONDITIONS
                with ur
                """)
      .targetDir("/etl/staging/PartID")
  })

  appendStep(PigTask { task =>
    task.setScript(TextFile.loadFromHdfs("/apps/scripts/PartIDProcess.pig"))
    Log.debug(task.script)
  })

}

/* Create an instance of the workflow builder to be used by the engine */

new PartIDWorkflow()

