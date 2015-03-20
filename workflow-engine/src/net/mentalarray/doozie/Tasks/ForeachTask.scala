package net.mentalarray.doozie.Tasks

import net.mentalarray.doozie.ValueProxy.TaskResult
import net.mentalarray.doozie.WorkflowTask


/**
 * Created by bgilcrease on 10/21/14.
 */

class ForeachTask(name: String) extends WorkflowTask(name) with Logging {

  private var _processCount: Int = 5

  private var _iteratorGen: Unit => Iterator[_] = null

  private var _iterator: Iterator[_] = null

  private var _nextValue: Unit => Option[Any] = { Unit => None }

  private var _executeSynchronously = false

  var doFunction: ( Any => WorkflowTask ) = null

  protected def assignIterator[T, V <: Iterable[T]](collection: TaskResult[V]): Unit = {
    val extractor = TaskResultReader(collection)
    _iteratorGen = Unit => { extractor.read.iterator }
    _nextValue = { Unit =>
      if (_iterator == null) { _iterator = _iteratorGen() }
      if( _iterator.hasNext ){
        Some(_iterator.next().asInstanceOf[T])
      } else {
        None
      }
    }
  }

  def forTask[T]( fn: T => WorkflowTask ) = {
    doFunction = { a: Any => fn(a.asInstanceOf[T]) }
  }

  def executeSynchronously : Boolean = _executeSynchronously
  def executeSynchronously_=(value: Boolean) = _executeSynchronously = value

  protected[workflow] def nextValue = _nextValue

  def processCount : Int = _processCount

  def processCount(processCount: Int) = {_processCount = processCount}


  override def validate: Unit = {
    if (_iteratorGen == null)
      throw new WorkflowStateException(this, "The iterable TaskResult must be specified and can not be null.")
    else if (doFunction == null)
      throw new WorkflowStateException(this, "The task to be executed for each iteration must be specified and can not be null.")
  }

}

object ForeachTask {

  def apply[T, V <: Iterable[T]](collection: TaskResult[V], fn: T => WorkflowTask ) = {
    val task = new ForeachTask("ForeachTask")
    task.assignIterator[T,V](collection)
    task.forTask(fn)
    task
  }

}
