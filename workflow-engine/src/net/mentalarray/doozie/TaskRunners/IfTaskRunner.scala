package net.mentalarray.doozie.TaskRunners

import net.mentalarray.doozie.Tasks.IfTask
import net.mentalarray.doozie.{Logging, TaskRunner}

/**
 * Created by bgilcrease on 10/22/14.
 */
class IfTaskRunner extends WorkflowTaskRunner with Logging {
  override type T = IfTask

  // Impl
  override protected def doAction(state: T): Boolean = {
    var success = true

    val runner = TaskRunner.getTaskRunner

    var taskName: String = null

    try {
      if( state.evalIf() ){
        val task = state.execThen
        taskName = task.name
        Log info ("Running If subtask (%s)" format taskName)
        success = runner.runTask(state.execThen)
      } else {
        if( state.hasElse ){
          val task = state.execElse
          taskName = task.name
          Log info ("Running If Else subtask (%s)" format taskName)
          success = runner.runTask(state.execElse)
        }
      }
    } catch {
      case ex: Exception => {
        Log error("Error running IfTask! with condition: %s" format state.evalIf)
        throw ex
      }
    }

    success
  }

  override protected def doTest(state: T): Unit = {
    state.validate
  }
}
