package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/9/14.
 */
trait StateHelpers { self : WorkflowTaskRunner =>

  type T <: WorkflowTask

  def setFromStateIfPresent[B <: AnyRef](selector: T => B, assigner: B => Unit)(implicit state: T) {
    val value = selector(state)
    if (value != Default.value[B])
      assigner(value)
  }

  def setFromState[B <: AnyRef](selector: T => B, assigner: B => Unit)(implicit state: T) {
    assigner(selector(state))
  }

}