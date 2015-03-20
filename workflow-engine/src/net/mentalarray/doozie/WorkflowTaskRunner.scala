package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/7/14.
 */

abstract class WorkflowTaskRunner {

  type T <: WorkflowTask

  // Testing
  final def test(task: WorkflowTask) {
    // Validate the task
    task.validate
    
    // Allow the runner to test-run the task
    doTest(task.asInstanceOf[T])
  }

  // Run
  final def run(task: WorkflowTask): Boolean = {

    // Validate the task
    task.validate

    // Result
    var result: Boolean = false

    // Task
    val internalTask = task.asInstanceOf[T]

    // Execute the action
    try {

      // Run the action
      result = doAction(internalTask)

    } catch {
      case e: Exception => {
        // Give the task an opportunity to handle the exception
        handleException(task, e)
        // Be responsible and rethrow the exception
        throw e
      }
    }

    // Return the task result (or true if we should ignore any errors)
    result || task.ignoreError
  }

  // Impl
  protected def doAction(state: T) : Boolean

  protected def doTest(state: T)

  protected[workflow] def handleException(task: WorkflowTask, ex: Exception) {  }

}
