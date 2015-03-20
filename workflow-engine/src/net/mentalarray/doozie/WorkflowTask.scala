package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/8/14.
 */
abstract class WorkflowTask protected[workflow](_name: String) {

  private var _ignoreError= false

  if (_name.isNullOrWhitespace)
    throw new WorkflowStateException(this, "Workflow name must be specified.")

  def validate : Unit

  def name: String = _name

  def ignoreError = _ignoreError
  def ignoreError_=(value: => Boolean) = _ignoreError = value

}

object WorkflowTask {
  type ResultsHandler[T] = (T) => Unit
}
