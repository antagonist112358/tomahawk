package net.mentalarray.doozie.tests

import net.mentalarray.doozie.{WorkflowException, WorkflowStateException}
import org.specs2.mutable.Specification

class HiveTaskRunnerSpec extends Specification {

  private def getInstance : HiveTaskRunner = {
    new HiveTaskRunner()
  }

  private def getTaskInstance : HiveTask = {
    new HiveTask("testTask")
  }

  "HiveTaskRunnerSpec" should {

    "throw an exception when trying to run an invalid task" in {
      getInstance.run(getTaskInstance) must throwA[WorkflowStateException]
    }

    "allow test-level execution where the command is not actually run" in {
      val task = getTaskInstance
      task.setNonQuery(List("""create table if not exists rawdata.data ( test varchar(20) )"""))
      getInstance.test(task) must not(throwA[WorkflowException])
    }

    "run a query on Hive DB " in {
      val task = getTaskInstance
      task.setNonQuery(List("""create table if not exists rawdata.data ( test varchar(20) )"""))
      getInstance.run(task) must not(throwA[WorkflowException])
    }


  }

}
