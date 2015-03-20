package net.mentalarray.doozie.Tasks

/**
 * Created by kdivincenzo on 10/23/14.
 */
class RunTaskSequenceTask extends AbstractCollectionTask("CollectionOfTasks") {
  private var _taskProcessor: WorkflowTask => Unit = null

  def setConfigurator(fn: WorkflowTask => Unit) = _taskProcessor = fn
  
  protected[workflow] def configurator : Option[WorkflowTask => Unit] = Option(_taskProcessor)

  def asParallel() : RunTaskSequenceParallelTask = this.asInstanceOf[RunTaskSequenceParallelTask]
}
