package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/9/14.
 */
class WorkflowStateException(state: WorkflowTask, message: String)
  extends WorkflowException(ExceptionHelper.createMessage(state.getClass, message)) {

}

class InvalidCommandLineArgument(message: String) extends WorkflowException(message) { }

class WorkflowException(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this(message, null)
}

object ExceptionHelper {
  def createMessage(stateClass: Class[_], msg: String): String = {
    "%s does not have valid state. Error: %s" format(stateClass.getName, msg)
  }
}


