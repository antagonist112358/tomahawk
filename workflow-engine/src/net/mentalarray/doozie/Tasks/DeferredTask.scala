package net.mentalarray.doozie.Tasks

import net.mentalarray.doozie.WorkflowTask

/**
 * Created by kdivincenzo on 10/22/14.
 */
class DeferredTask(generator: Unit => WorkflowTask) extends WorkflowTask("TaskUsingResults") {

  private val _taskGenerator: Unit => WorkflowTask = generator

  override def validate: Unit = {
    if (_taskGenerator == null)
      throw new WorkflowStateException(this, "The task which uses a deferred result set must be defined (can not be null).")
  }

  protected[workflow] def makeTask : WorkflowTask = _taskGenerator()
}

object DeferredTask {
  def apply(fn: => WorkflowTask) = {
    new DeferredTask( _ => fn)
  }
}