package net.mentalarray.doozie.TaskRunners

import java.util.concurrent.Executors

import net.mentalarray.doozie.Tasks.ForeachTask
import net.mentalarray.doozie.{TasksSequence, WorkflowTask}

import scala.concurrent._
import scala.util.{Failure, Success}


/**
 * Created by bgilcrease on 10/21/14.
 */


class ForeachTaskRunner extends WorkflowTaskRunner with Logging with FortuneTeller[Boolean] {
  override type T = ForeachTask

  def poolTest(threadPool: Int) = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadPool))

  private var pool = 5

  override implicit lazy val ec = poolTest(threadPool = pool)

  override protected def doAction(state: T): Boolean = {

    var success = true

    try {

      // Convert to collection
      val foreachTasks = extractTasks(state)

      success = state.executeSynchronously match {
        case true =>
          Log debug ("Executing Foreach expansion synchronously...")
          runTasksSync(foreachTasks)
        case _ =>
          Log debug ("Executing Foreach expansion asynchronously...")
          pool = state.processCount
          runTasksAsync(foreachTasks)
      }

    } catch {
      case ex: Exception => {
        Log error ("Error running Foreach subtask")
        throw ex
      }
    }

    success
  }

  override protected def doTest(state: T): Unit = {
    state.validate
  }

  private def extractTasks(foreach: ForeachTask) : TasksSequence = {
    var output = new TasksSequence
    var current = foreach.nextValue()

    while (!current.isEmpty) {
      output += foreach.doFunction(current.get)
      current = foreach.nextValue()
    }

    output
  }

  private def runTasksSync(tasks: TasksSequence) : Boolean = {
    val runner = TaskRunner.getTaskRunner

    var success = true
    var count = 0
    var taskName = ""
    var lastTask: WorkflowTask = null

    try {

      for (task <- tasks) {
        if (success) {
          lastTask = task
          taskName = task.name
          Log info ("Running Foreach subtask #%d: %s" format(count, taskName))
          success = runner.runTask(task)
          Log info ("Foreach subtask #%d (%s) completed in %s"
            format(count,
            taskName,
            success match {
              case true => "success"
              case _ => "failure"
            }))
          count += 1
        }
      }

      success

    } catch {
      case ex: Exception => {
        Log error ("Error running Foreach subtask #%d (%s) for iteration Option: %s " format(count, taskName, lastTask.toString()))
        throw ex
      }
    }
  }

  private def runTasksAsync(tasks: TasksSequence) : Boolean = {


    var futures = List.empty[Future[Boolean]]

    // Lock the temporary directory
   // Application.temporaryManager.lockTemporaryDirectory

    for (task <- tasks) {

      futures ::= async {
        Log info ("Running Foreach subtask: %s" format task.name)
        val success = TaskRunner.getTaskRunner.runTask(task)
        Log info ("Foreach subtask %s completed in %s" format(
          task.name,
          success match {
              case true => "success"
              case _ => "failure"
            }
          ))
        success
      }

    }

    Log info "\n\t*** Waiting for all parallel tasks to finish ***\n"

    try {
      // Await all the tasks completing (or if an exception occurs)
      awaitAllFutures(futures) match {
        case Success(results) =>
          Log info "All parallel tasks completed successfully."
          results.reduce((r1, r2) => r1 & r2)
        case Failure(ex) => throw ex
      }
    } finally {
     // Application.temporaryManager.unlockTemporaryDirectory
      Application.temporaryManager.clearTemporaryDirectory
    }
  }

}
