/**

 */
package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 10/2/14.
 */

import scala.collection.mutable

/**
 * Trait which abstracts different types of command line switches.
 */
sealed trait ShellArgument {
  def toTuple: (String, String)
}

/**
 * Represents a text-literal switch
 * @param text The exact text of the command line switch
 */
case class ExactSwitch(text: String) extends ShellArgument {
  override def toTuple = text -> ""
}

/**
 * Represents a non-formatted switch. What this means is that the switch name does not include any
 * prependations, such as the usual single dash.
 * @param text The name of the switch, without any leading dash.
 */
case class Switch(text: String) extends ShellArgument {
  override def toTuple = text -> ""
}

/**
 * Represents a command line parameter. Parameters are switches which also allow for values to be passed
 * into the application.
 * @param name
 * @param value
 */
case class Param(name: String, value: String) extends ShellArgument {
  override def toTuple = name -> value
}

class Arguments {

  private val argsMap = mutable.LinkedHashMap.empty[String, String]

  def addExact(key: String) = {
    argsMap += key -> null
    this
  }

  def add(keyValue: (String, String)) = {
    argsMap += getKey(keyValue._1) -> keyValue._2
    this
  }

  def add(key: String, value: String) = {
    argsMap += getKey(key) -> value
    this
  }

  def add(single: String) = {
    argsMap += getKey(single) -> null
    this
  }

  def ++(otherArgs: => Arguments) = {
    otherArgs.argsMap.foreach(argsMap += _)
    this
  }

  def toShellArgs : Array[String] = toString.tokenize.toArray

  override def toString : String = {
    val builder = new StringBuilder

    for((key, value) <- argsMap) {
      builder ++= key; builder += ' '
      if (!value.isNullOrWhitespace)
        builder ++= value; builder += ' '
    }

    builder.toString.trim
  }

  private def getKey(s: String) = if (s.startsWith("--")) s else "--" + s
}

object Arguments {
  def apply(parameters: ShellArgument*) : Arguments = {
    val args = new Arguments
    parameters.foreach(_ match {
      case exact: ExactSwitch => args.addExact(exact.toTuple._1)
      case s: Switch => args.add(s.toTuple._1)
      case p: Param => args.add(p.toTuple)
    })
    args
  }
}