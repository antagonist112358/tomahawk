package net.mentalarray.doozie

import net.mentalarray.doozie.Tasks._

import scala.collection.mutable

/**
 * Created by kdivincenzo on 9/9/14.
 */

class TaskRunner(availableRunners: Map[String, WorkflowTaskRunner]) extends Logging {

  private def getRunnerForTask(task: WorkflowTask) = availableRunners.get(task.getClass.getName) match {
    // Have a runner
    case Some(runner) => runner
    // Unknown task type
    case None => throw new WorkflowException("Unknown task type. Cannot build runner for task type: %s" format task.getClass.getName)
  }

  def runTask(task: WorkflowTask) : Boolean = try {

    // Get the runner for the task
    val taskRunner = getRunnerForTask(task)

    // Run the task
    taskRunner.run(task)

  } catch {
      case ex: Exception => Log.error("Encountered error while trying to run task '%s'" format task.name, ex)
      false
  }


  def runTasks(tasks: Seq[WorkflowTask]): Unit = {
    for (task <- tasks) {
      try {
        // Debug
        Log debug String.format("Running task: %s", task.name)

        // Grab the executor for this task and execute the task
        val taskRunner = getRunnerForTask(task)

        // Execute
        taskRunner.run(task)

        // Trace
        Log debug "Task %s completed".format(task.name)

      } catch {
        case ex: Throwable => Log error(String.format("Encountered an error while trying to execute tasks: %s", ex.getMessage), ex)
      }
    }

  }

}

object TaskRunner {

  private val runnersMap = mutable.Map.empty[String, Unit => WorkflowTaskRunner]
  private val lockerObject = new Object

  // Register all the tasks and runners here:
  runnerForTask(classOf[SqoopTask])     { _ => new SqoopTaskRunner }
  runnerForTask(classOf[BashTask])      { _ => new BashTaskRunner }
  runnerForTask(classOf[ScaldingTask])  { _ => new ScaldingTaskRunner }
  runnerForTask(classOf[HdfsTask])      { _ => new HdfsTaskRunner }
  runnerForTask(classOf[PigTask])       { _ => new PigTaskRunner }
  runnerForTask(classOf[HiveTask])      { _ => new HiveTaskRunner }
  runnerForTask(classOf[ScalaTask])     { _ => new ScalaTaskRunner }
  runnerForTask(classOf[ForeachTask])   { _ => new ForeachTaskRunner }
  runnerForTask(classOf[SparkTask])     { _ => new SparkTaskRunner }
  runnerForTask(classOf[FileBuilderTask]){ _ => new FileBuilderRunner}
 // runnerForTask(classOf[IfTask])        { _ => new IfTaskRunner }
  runnerForTask(classOf[RunTaskSequenceTask]){ _ => new TaskCollectionProcessor }
  runnerForTask(classOf[RunTaskSequenceParallelTask]){ _ => new ParallelTaskCollectionProcessor }

  private def runnerForTask(classType: Class[_])(generator: Unit => WorkflowTaskRunner): Unit = {
    runnersMap += (classType.getName -> generator)
  }

  private def toRunnerResolver(tasksMap: mutable.Map[String, Unit => WorkflowTaskRunner]) = {
    tasksMap.map {pair: (String, Unit => WorkflowTaskRunner) =>
      pair._1 -> pair._2()
    }.toMap
  }

  def getTaskRunner : TaskRunner = {
    lockerObject.synchronized {
      new TaskRunner(toRunnerResolver(runnersMap))
    }
  }


}