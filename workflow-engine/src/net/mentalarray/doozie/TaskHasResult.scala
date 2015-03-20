package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/9/14.
 */

object TaskHasResult {
  implicit class WorkflowTaskWithResult[A](task: WorkflowTask with TaskHasResult[A]) {
    def get: A = task.getResult
  }
}

trait TaskHasResult[A] { self: WorkflowTask =>

  private var _result: A = Default.value[A]

  protected[workflow] def setResult(result: A) : Unit = {
    _result = result
  }

  protected def getResult : A = _result

}
