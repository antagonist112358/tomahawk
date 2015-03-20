package net.mentalarray.doozie.Builders

/**
 * Created by kdivincenzo on 9/9/14.
 */

abstract class TaskBuilder(_taskName: String) {

  // The abstract type of Task that this builder constructs
  type TTask <: WorkflowTask

  // Instance of the WorkflowTask
  protected final val task = composeTask(_taskName)

  // Stores the optional components of the WorkflowStep
  private var _onError: TasksSequence = null
  private var _always: TasksSequence = null

  // Should return the provided abstract type instance, configured
  final def buildStep : WorkflowStep = {
    WorkflowStep(task, Some(_onError), Some(_always))
  }

  // Helper methods
  def setOnError(onError: => TasksSequence): Unit = {
    _onError = onError
  }

  def setAlways(always: => TasksSequence): Unit = {
    _always = always
  }

  // Must be implemented by the implementing class
  protected def composeTask(name: String) : TTask


}
