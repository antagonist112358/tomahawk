package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/30/14.
 */
case class WorkflowStep(action: WorkflowTask, onError: Option[TasksSequence], always: Option[TasksSequence])

// Value object for WorkflowStep (entry in TaskGraph or TaskSequence)
object WorkflowStep {

  def apply(action: WorkflowTask) : WorkflowStep = {
    WorkflowStep(action, None, None)
  }

  def apply(action: WorkflowTask, onError: TasksSequence) : WorkflowStep = {
    WorkflowStep(action, Some(onError), None)
  }

  def apply(action: WorkflowTask, onError: TasksSequence, always: TasksSequence) : WorkflowStep = {
    WorkflowStep(action, Some(onError), Some(always))
  }
}
