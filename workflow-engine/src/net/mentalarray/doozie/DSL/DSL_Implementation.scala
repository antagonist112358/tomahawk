package net.mentalarray.doozie.DSL

/**
 * Created by kdivincenzo on 11/24/14.
 */

abstract class AbstractCommand {
  def commandText : TaskResult[String]
  def ignoreErrors : Boolean
}

case class Command(commandText: TaskResult[String], ignoreErrors : Boolean = true)
  extends AbstractCommand


