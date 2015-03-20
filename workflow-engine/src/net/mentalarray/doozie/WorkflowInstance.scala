package net.mentalarray.doozie

import net.mentalarray.doozie.DBStore.DBStatus
import net.mentalarray.doozie.Internal.Timer
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{DateTime, Interval}

import scala.util.control.Breaks._

/**
 * Created by kdivincenzo on 9/9/14.
 */

class WorkflowInstance(wfName: String, graph: ExecutionGraph) extends Logging {

  private object Time {
    private val _dateFormatter = new java.text.SimpleDateFormat("HH:mm:ss")
    def current = _dateFormatter.format(java.util.Calendar.getInstance().getTime)
  }

  private class Stopwatch {
    private val _startTime = DateTime.now
    private lazy val formatter = {
      new PeriodFormatterBuilder()
        .printZeroAlways()
        .appendMinutes()
        .appendSuffix("m")
        .appendSeparator(" ")
        .appendSeconds()
        .appendSuffix("s")
        .toFormatter
    }

    private def now = DateTime.now

    def interval = new Interval(_startTime, now).toPeriod

    def intervalString = formatter.print(interval)
  }

  private val instanceId = java.util.UUID.randomUUID.toString

  private def makeHeartbeatTimer(stp: Stopwatch): Timer = {
    Timer.startNew(5000) {
      println("Heartbeat - Task time: %s" format stp.intervalString)
      println("Number of active threads: %d" format java.lang.Thread.activeCount())
    }
  }

  Log debug String.format("Creating workflow %s with %s tasks...", wfName, graph.size.toString)

  // Properties
  def name: String = wfName
  def id: String = instanceId
  
  // Methods
  def execute {

    Log info("Executing workflow %s (instance id: %s) with %d tasks..." format(name, id, graph.size))

    var hasError: Boolean = false
    var taskCount: Integer = 0
    var currentStep: WorkflowStep = null

    // Create a runner
    val taskExecutor = TaskRunner.getTaskRunner

    // Get our iterator
    val iterator = graph.executionIterator

    // Start executing positively asserted tasks
    breakable {
      for (activeStep <- iterator) {
        // Set the value
        currentStep = activeStep

        // Debug
        Log info("Running task #%d: %s" format(taskCount + 1, activeStep.action.name))

        // Create a stopwatch
        val swatch = new Stopwatch()

        // Log to db
        //startTask("%s #%d" format (activeStep.action.name, taskCount))
        val timer = startTask(activeStep.action.name, swatch)

        // Grab the executor for this task and execute the task
        hasError = !taskExecutor.runTask(activeStep.action)
      
        // Debug
        Log info("Task #%d (%s) completed in %s" format((taskCount + 1), activeStep.action.name, if (!hasError) "success" else "failure"))

        //Log to db
        endTask(activeStep.action.name, !hasError, timer)

        // If the result is false, we hit an error in the task
        if (hasError) break
        
        // Increment
        taskCount += 1

        // Clear the temporary directory (which will recreate if deleted)
        Log debug("Clearing temporary directory's contents...")
        Application.temporaryManager.clearTemporaryDirectory
      } 
    }

    // Handle errors
    if (!hasError)
    {
      Log info String.format("Workflow %s completed successfully (%d tasks executed).", name, taskCount)
    } else {
      Log warn String.format("Workflow %s terminated due to an error.", name)
      if (!currentStep.onError.isEmpty) {
        Log info "Running 'onError' tasks..."
        taskExecutor.runTasks(currentStep.onError.get)
      }        
    }

    // Execute finalizers
    Log debug String.format("Executing cleanup tasks...")
    taskExecutor.runTasks(iterator.getFinalizers)

    Log debug String.format("Workflow graph: %s (instance id: %s) - execution completed.", name, id)
  } 

  protected def startTask(taskName: String, stp: Stopwatch) = {
    setStatus(name, taskName, "Running")
    makeHeartbeatTimer(stp)
  }

  protected def endTask(taskName: String, success: Boolean, t: Timer) = {
    setStatus(name, taskName, if (success) "Pass" else "Fail")
    t.stop
  }

  private def setStatus(jobName: String, taskName: String, status: String) {
    // If we can't set status, trap the ex but continue executing
    try {
      DBStatus.setStatus(jobName, taskName, status)
    } catch {
      case ex: Throwable => Log error(String.format("Could not set JobStatus in database for job: %s (task: %s)", jobName, taskName), ex)
    }
  }

}
