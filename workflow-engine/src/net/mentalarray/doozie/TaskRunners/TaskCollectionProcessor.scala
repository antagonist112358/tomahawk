package net.mentalarray.doozie.TaskRunners

/**
 * Created by kdivincenzo on 10/23/14.
 */
class TaskCollectionProcessor extends WorkflowTaskRunner with Logging {
  override type T = RunTaskSequenceTask

  // Impl
  override protected def doAction(state: RunTaskSequenceTask): Boolean = {
    Log debug("Executing TaskSequence with %d tasks." format state.length)

    // Create a taskRunner
    val runner = TaskRunner.getTaskRunner

    var result = true

    for(task <- state) {
      try {
        if (result) {
          // Configure the task if there is a configuration function
          state.configurator.ifNotNoneAction(cfg => cfg(task))
          // Run the task and annotate the result. Continue if we continueOnError or if the task was successful
          result = runner.runTask(task)
          val resultTxt = if (result) "success" else "failure"
          Log debug("Task resulted in %s (TaskSequence continues on individual task error? %s" format(resultTxt, state.continueOnError.toString))
          // Continuation based on task result or continueOnError
          result |= state.continueOnError
        } else
          Log debug("Skipping task %s due to previous task failure." format task.name)
      } catch {
        case ex: Exception if (task.ignoreError) => Log warn("Continue on error task %s threw an exception." format task.name, ex); result = false
        case e: Throwable => throw e
      }
    }

    // Result
    result
  }

  override protected def doTest(state: T): Unit = {
    Log debug("Evaluating TaskSequence with %d tasks." format state.length)
  }
}
