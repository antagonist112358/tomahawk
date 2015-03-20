
import net.mentalarray.doozie.Builders.WorkflowBuilder
import net.mentalarray.doozie.Logging
import net.mentalarray.doozie.Tasks._


/**
 * Created by bgilcrease on 10/10/14.
 */


/* Create a WorkflowBuilder class with the steps for your job added through appendStep()


Current support tasks can be found in net.mentalarray.doozie.Tasks
and are:
BashTask
HdfsTask
HiveTask
PigTask
ScalaTask
SqoopTask

Each task is ran through a task runner.  See the examples below on how to add tasks to a job

 */

class BasicExample extends WorkflowBuilder("BasicExample") with Logging {

  appendStep( HiveTask{ task =>
    task.setNonQuery("create table bryang(test1 string)")
  })

}

/* Create an instance of the workflow builder to be used by the engine */

new BasicExample()

