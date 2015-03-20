package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/15/14.
 */
class BashTaskRunnerSpec extends Specification {

  private def getInstance : BashTaskRunner = {
    new BashTaskRunner()
  }

  private def getTaskInstance : BashTask = {
    new BashTask("testTask")
  }

  "BashTaskRunnerSpec" should {

    "throw an exception when trying to run an invalid task" in {
      getInstance.run(getTaskInstance) must throwA[WorkflowStateException]
    }

    "allow test-level execution where the command is not actually run" in {
      val task = getTaskInstance
      task.setCommand("beep")
      getInstance.test(task) must not(throwA[WorkflowException])
    }



  }

}
