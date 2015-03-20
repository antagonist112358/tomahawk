package net.mentalarray.doozie.tests

/**
 * Created by kdivincenzo on 9/24/14.
 */

class FakeWorkflowTask extends WorkflowTask("l33tTaskYo") {
  private var _setting: Int = 0
  private var _requiredSetting: String = null

  def setting: Int = _setting
  def setting_=(value: => Int) = _setting = value

  def required: String = _requiredSetting
  def required_=(value: => String) = _requiredSetting = value

  override def validate: Unit = {
    if (_requiredSetting.isNullOrWhitespace) {
      throw new WorkflowException("Required value not set.")
    }
  }
}
/*
class FakeBuilder extends TaskBuilder("BobTheBuilder") with Builder {

  override type TTask = FakeWorkflowTask

  // Must be implemented by the implementing class
  override protected def composeTask(name: String): TTask = new FakeWorkflowTask

  // Setters used during configuration
  def setting(value: => Int) = {
    this.task.setting = value
    this
  }
  def required(value: => String) = {
    this.task.required = value
    this
  }

}

object FakeBuilder {
  def apply(fn: FakeBuilder => Unit): FakeBuilder = {
    val builder = new FakeBuilder
    fn(builder)
    builder
  }
}

*/