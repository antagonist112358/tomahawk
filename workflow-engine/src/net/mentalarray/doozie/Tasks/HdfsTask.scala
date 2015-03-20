package net.mentalarray.doozie.Tasks

/**
 * Created by kdivincenzo on 9/25/14.
 */
// TODO: Finish this stupid class!
class HdfsTask(name: String) extends AbstractCommandTask(name) with TaskHasResult[String] {

  override def validate {
    if (_command.isNullOrWhitespace)
      throw new WorkflowStateException(this, "The HDFS command must be specified.")
  }

}

object HdfsTask {
  def apply(cfgFn: HdfsTask => Unit): HdfsTask = {
    val task = new HdfsTask("Execute Hdfs Command")
    cfgFn(task)
    task
  }
}