package net.mentalarray.doozie

import scala.collection.mutable

/**
 * Created by kdivincenzo on 9/9/14.
 */

class ExecutionGraph extends mutable.Iterable[WorkflowStep] {

  private val _internalList = mutable.ListBuffer.empty[WorkflowStep]

  // List like operations
  def +=(step: WorkflowStep) : ExecutionGraph = {
    _internalList += step
    this
  }

  def ++(steps: Seq[WorkflowStep]) : ExecutionGraph = {
    _internalList ++ steps
    this
  }

  def executionIterator : ExecutionGraphIterator = {
    new ExecutionGraphIterator(this)
  }

  override def iterator: Iterator[WorkflowStep] = _internalList.iterator
}

class ExecutionGraphIterator(source: ExecutionGraph) extends Iterator[WorkflowStep] {

  private val executionSequence = source.toList
  private val executedTasks = mutable.MutableList.empty[WorkflowStep]
  private var index: Int = 0
    

  // Execution graph implementation
  def head: Option[WorkflowTask] = {
    if (executionSequence.size == 0) None else Some(executionSequence(0).action)
  }

  def getFinalizers: Seq[WorkflowTask] = {
    val tasks = new TasksSequence()

    executedTasks.foreach(t =>
      if (!t.always.isEmpty) {
        tasks ++ t.always
      }
    )

    tasks
  }

  override def next(): WorkflowStep = {
    // Bounds checking
    if (!hasNext) throw new IndexOutOfBoundsException("No additional WorkflowEntry items exist in ExecutionGraph.")

    // Get element at position
    val step = executionSequence(index)

    // Assign
    executedTasks += step

    // Increment
    index += 1

    // Return
    step
  }

  override def hasNext: Boolean = (index < executionSequence.length)

}
