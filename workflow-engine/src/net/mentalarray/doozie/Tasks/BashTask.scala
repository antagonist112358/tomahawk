package net.mentalarray.doozie.Tasks

/**
 * Created by kdivincenzo on 9/9/14.
 */
class BashTask(name: String) extends AbstractCommandTask(name) with TaskHasResult[String] {

  override def validate {
    if (_command.isNullOrWhitespace)
      throw new WorkflowStateException(this, "This shell execution string must be specified.")
  }

}

object BashTask {
  def apply(cfgFn: BashTask => Unit) = {
    val task = new BashTask("Execute Shell Command")
    cfgFn(task)
    task
  }
}
