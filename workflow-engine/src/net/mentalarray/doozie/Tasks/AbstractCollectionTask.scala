package net.mentalarray.doozie.Tasks

/**
 * Created by kdivincenzo on 1/5/2015.
 */
abstract class AbstractCollectionTask(name: String) extends WorkflowTask(name) with Traversable[WorkflowTask] {
  private var _tasks: TasksSequence = new TasksSequence
  private var _continueOnError: Boolean = false


  def addTask(task: WorkflowTask) = _tasks += task

  def continueOnError = _continueOnError
  def continueOnError_=(value: Boolean) = _continueOnError = value

  def length = _tasks.length

  override def validate: Unit = {
    if (_tasks.length == 0) throw new WorkflowStateException(this, "No tasks in collection.")
  }

  override def foreach[U](f: (WorkflowTask) => U): Unit = _tasks.foreach(f)
}
