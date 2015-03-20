package net.mentalarray.doozie.DSL

import net.mentalarray.doozie.WorkflowTask

/**
 * Created by kdivincenzo on 10/24/14.
 */

trait DeferredReader {
  def extract[U](result: TaskResult[U]) : U = {
    val reader = new TaskResultReader(result)
    reader.read
  }
}

object deferredResult {

  def apply[U](resGen: DeferredReader => U) : TaskResult[U] = {
    val reader = new DeferredReader {}
    TaskResult[U](_ => resGen(reader))
  }

}

object taskWithResults {

  def apply(taskGen: DeferredReader => WorkflowTask) : DeferredTask = {
    val reader = new DeferredReader {}
    DeferredTask(taskGen(reader))
  }

}

