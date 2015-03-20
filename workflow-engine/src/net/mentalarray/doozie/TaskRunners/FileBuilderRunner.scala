package net.mentalarray.doozie.TaskRunners

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}


/**
 * Created by bwilson on 12/16/14.
 */
class FileBuilderRunner extends WorkflowTaskRunner with Logging {

  type T = FileBuilderTask

  override def doAction(task: FileBuilderTask): Boolean = {

    implicit val s: FileBuilderTask = task

    val config: Configuration = new Configuration()

    def SysMatchTest(x: String): FileSystem = x match {
      case "hdfs" => FileSystem.get(config)
      case "local" => FileSystem.getLocal(config)
    }

    val srcSys = SysMatchTest(task.srcSys)
    val destSys = SysMatchTest(task.destSys)

    try {
      FileUtil.copyMerge(srcSys, new Path(task.inPath), destSys, new Path(task.outPath),
        task.srcCheckDel, config, task.stringAdd)
    } catch {
      case ex: Exception => {
        Log error "Error merging directory into single file"
        throw ex
      }
    }
  }

  override protected def doTest(state: FileBuilderTask): Unit = {
    try {
      state.validate
    } catch {
      case ex: Throwable => Log error ("Invalid task state: %s" format ex.getMessage)
    }
  }

  Log info "FileBuilder executing on data set"
}


