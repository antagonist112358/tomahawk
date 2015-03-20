package net.mentalarray.doozie.TaskRunners

/**
 * Created by kdivincenzo on 10/22/14.
 */
class DeferredTaskRunner extends WorkflowTaskRunner with Logging {

  override type T = DeferredTask

  private def getWorkflowTaskAndRunner(wrapper: T) = {
    val task = wrapper.makeTask
    (task, TaskRunner.getTaskRunner)
  }

  // Impl
  override protected def doAction(state: T): Boolean = {
    val (task, runner) = getWorkflowTaskAndRunner(state)
    runner.runTask(task)
  }

  override protected def doTest(state: T): Unit = {
    val (task, runner) = getWorkflowTaskAndRunner(state)
    if (task == null) throw new WorkflowException("Inner task is null on creation.")
    else if (runner == null) throw new WorkflowException("Task has no associated TaskRunner.")
  }
}
