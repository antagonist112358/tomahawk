package net.mentalarray.doozie.TaskRunners

import net.mentalarray.doozie.Application
import net.mentalarray.doozie.Setting.HDFSBin
import net.mentalarray.doozie.Tasks.HdfsTask
import net.mentalarray.doozie.Utility.Path

import scala.sys.process._

/**
 * Created by kdivincenzo on 9/9/14.
 */
class HdfsTaskRunner extends WorkflowTaskRunner with Logging {

  type T = HdfsTask

  private lazy val hdfsCmd = {
    val path = Application.settings.getSetting(HDFSBin)
    Path.combine(path, "hdfs")
  }

  // Impl
  override protected def doAction(state: HdfsTask): Boolean = {

    val finalCmd = hdfsCmd + " dfs -" + state.command

    Log debug("HDFS command which will be executed: %s" format finalCmd)

    val out = new StringBuilder
    val resultCode = finalCmd ! ProcessLogger((line: String) => out.append(line + '\n'))

    state.setResult(out.toString())

    (resultCode == 0)
  }

  override protected def doTest(state: HdfsTask): Unit = {

    try {
      state.validate
    } catch {
      case ex: Throwable => Log error("Invalid task state: %s" format(ex.getMessage))
    }

    Log info("HDFS command which will be executed: %s" format state.command)
  }
}
