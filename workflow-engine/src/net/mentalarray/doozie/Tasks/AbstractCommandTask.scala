package net.mentalarray.doozie.Tasks

/**
 * Created by kdivincenzo on 10/24/14.
 */
abstract class AbstractCommandTask(name: String) extends WorkflowTask(name) {

  protected var _command: String = null
  private var _replacer : Option[ReplacementParameters] = None

  def command: String = _replacer match {
    case Some(replacer) => replacer.formatText(_command)
    case _ => _command
  }

  def setCommand (value: String) {
    _command = value
  }

  def setReplacements(replacer: ReplacementParameters) = _replacer = Some(replacer)

}
