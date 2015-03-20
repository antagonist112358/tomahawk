package net.mentalarray.doozie.Builders

import net.mentalarray.doozie.ValueProxy.TaskResult
import net.mentalarray.doozie.{WorkflowStep, WorkflowTask}

/**
 * Created by kdivincenzo on 9/9/14.
 */
class WorkflowBuilder(_wfName: String) extends Builder[WorkflowInstance] {

  private val graph: ExecutionGraph = new ExecutionGraph
  private var _inputPath: String = null

  def inputPath: String = _inputPath
  def setInputPath(path: String) = _inputPath = path

  protected var workflowName = _wfName

  def this() {
    this("{Unnamed Workflow}")
  }

  def appendStep(step: WorkflowStep) : WorkflowBuilder = {
    graph += step
    this
  }

  def appendStep(task: WorkflowTask) : WorkflowBuilder = {
    graph += WorkflowStep(task)
    this
  }

  def getResultFrom[T](task: WorkflowTask with TaskHasResult[T]): TaskResult[T] = {
    graph += WorkflowStep(task)
    TaskResult[T](_ => task.get)
  }

  def build : WorkflowInstance = {
    new WorkflowInstance(_wfName, graph)
  }


}