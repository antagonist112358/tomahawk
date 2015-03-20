package net.mentalarray.doozie.tests

import org.specs2.mutable.Specification

/**
 * Created by kdivincenzo on 9/8/14.
 */
class WorkflowTaskRunnerSpec extends Specification {

  // Dummy state
  private class DummyTask extends WorkflowTask("DummyTask") {

    private var wasValidateCalled: Boolean = false

    override def validate {
      wasValidateCalled = true
    }

    def validateCalled = wasValidateCalled
  }

  // Mocked WorkflowState
  private def getWorkflowTask: WorkflowTaskRunner = {

    return new WorkflowTaskRunner {
      type T = DummyTask

      override def doAction(state: DummyTask): Boolean = {
        return true
      }
      override def doTest(state: DummyTask) { }
    }

  }

  "WorkflowTask" should {

    // Validate state on test
    "validate state when test" in {
      val state = new DummyTask()
      val wf = getWorkflowTask
      wf.test(state)

      state.validateCalled must beEqualTo(true)
    }

    // Validate state on run
    "validate state when run" in {
      val state = new DummyTask()
      val wf = getWorkflowTask
      wf.run(state)

      state.validateCalled must beEqualTo(true)
    }


  }

}
