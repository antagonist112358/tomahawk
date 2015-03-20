package net.mentalarray.doozie.TaskRunners

import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
 * Created by kdivincenzo on 10/23/14.
 */
class ParallelTaskCollectionProcessor extends WorkflowTaskRunner with Logging with FortuneTeller[Boolean] {
  override type T = RunTaskSequenceParallelTask

  // Impl
  override protected def doAction(state: RunTaskSequenceParallelTask): Boolean = {
    Log debug("Asynchronously executing TaskSequence with %d tasks." format state.length)

    var futures = List.empty[Future[Boolean]]

    Application.temporaryManager.lockTemporaryDirectory

    for(task <- state) {
      futures ::= async {
        // Create a taskRunner
        val runner = TaskRunner.getTaskRunner
        // Log
        Log debug ("[Async] Executing task: %s" format task.name)
        // Execute the task with the runner
        runner.runTask(task)
      }
    }

    var result = true

    try {
      awaitAllFutures(futures) match {
        case Success(results) => for (r <- results) {
          result &= r
          Log debug ("Task resulted in %s (TaskSequence continues on individual task error? %s)" format(if (result) "success" else "failure", state.continueOnError.toString))
          result |= state.continueOnError
        }

        // Result
        result
        case Failure(t) => throw t
      }
    } finally {
      Application.temporaryManager.unlockTemporaryDirectory
      Application.temporaryManager.clearTemporaryDirectory
    }
  }

  override protected def doTest(state: T): Unit = {
    Log debug("Evaluating TaskSequence with %d tasks." format state.length)
  }
}
