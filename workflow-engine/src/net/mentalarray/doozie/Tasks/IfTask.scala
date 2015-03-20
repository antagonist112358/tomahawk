package net.mentalarray.doozie.Tasks

import net.mentalarray.doozie.WorkflowTask

/**
 * Created by bgilcrease on 10/22/14.
 */
class IfTask(name: String) extends WorkflowTask(name) with Logging {

  private var _if : (Unit => Boolean) = null
  private var _then: WorkflowTask = null
  private var _else: WorkflowTask = null

  def evalIf = _if
  def execThen = _then

  def execElse = {
    if (hasElse) {
      _else
    } else {
      throw new WorkflowStateException(this, "The condiational, if statement, and else statement must be defined for execElse")
    }
  }

  def hasElse: Boolean = {
    if( _else == null ) false
    else true
  }


  override def validate: Unit = {
    if( _if == null || _then == null ) {
      throw new WorkflowStateException(this, "The condiational and if statement must be defined for an ifTask" )
    }
  }

}

object IfTask {

  def apply(condition: => TaskResult[Boolean], ifStatement: WorkflowTask, elseStatement: WorkflowTask ) = {
    val task = new IfTask("IfTask")
    task._if = { _ => new TaskResultReader(condition).read }
    task._then = ifStatement
    task._else = elseStatement
    task
  }

  def apply(condition: TaskResult[Boolean], ifStatement: WorkflowTask) = {
    val task = new IfTask("IfTask")
    task._if = { _ => new TaskResultReader(condition).read }
    task._then = ifStatement
    task
  }

}
