package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 9/29/14.
 */

object ApplicationOptions {

  private val switches = Switches.allSwitches
      //List[CmdLineSwitch](new JarFileSwitch, new InstanceNameSwitch, new ScalaFileSwitch, new LogToConsoleSwtich)

  private def isListEmpty(list: List[String]) : Boolean = (list == null || list.length == 0)

  private def isSwitch(s: String) : Boolean = (s.length > 2 && s.take(2) == "--")

  private def parseArgsInternal(map: Map[String, Any], list: List[String]) : Map[String, Any] = {
    // If there are no more arguments to match, return the values map
    if (isListEmpty(list)) return map

    // Extract the first argument of the list off and lowerCase it if it is a switch. This will allow for case insensitive matching on switch names.
    val remainingArgs = list match {
      case head :: remainder if isSwitch(head) => List(head.toLowerCase) ++ remainder
      case _ => list
    }

    // If there are more arguments left, we require at least one of them to match
    // Check each switch for a valid match
    for (switch <- switches) switch.check(map, remainingArgs) match {
        case Some(result) => return parseArgsInternal(result._1, result._2)
        case None => /* Do Nothing */ ;
    }

    // No matches, so we have an unknown switch
    throw new InvalidCommandLineArgument("Unknown or improperly used argument: %s" format list(0))
  }

  def parseArgs(args: Seq[String]) : Map[String, Any] = {
    parseArgsInternal(Map(), args.toList)
  }
}

trait CmdLineSwitch {
  type OptionMap = Map[String,Any]

  def check(map: OptionMap, remainingArgs: List[String]) : Option[(OptionMap, List[String])]
}

abstract class NoArgumentSwitch(param: String, key: String) extends CmdLineSwitch {

  private lazy val switchName : String = "--" + param

  override def check(map: OptionMap, remainingArgs: List[String]): Option[(OptionMap, List[String])] = remainingArgs match {
    case switch :: Nil    if (switch == switchName)     => Some(map ++ Map(key -> true), List())
    case switch :: tail   if (switch == switchName)     => Some(map ++ Map(key -> true), tail)
    case tail => None
  }
}

abstract class SingleArgumentSwitch(param: String, key: String) extends CmdLineSwitch {

  private lazy val switchName : String = "--" + param

  protected def checkArgument(arg: String) : Boolean = true

  protected def extractValue(arg: String) = arg

  override def check(map: OptionMap, remainingArgs: List[String]): Option[(OptionMap, List[String])] = remainingArgs match {
    case switch :: arg :: Nil    if (switch == switchName && checkArgument(arg))     => Some(map ++ Map(key -> extractValue(arg)), List())
    case switch :: arg :: tail   if (switch == switchName && checkArgument(arg))     => Some(map ++ Map(key -> extractValue(arg)), tail)
    case tail => None
  }
}

/*
class JarFileSwitch extends CmdLineSwitch {
  private def isJarFile(s : String): Boolean = (s.trim.endsWith(".jar"))
  override def check(map: OptionMap, remainingArgs: List[String]): Option[(OptionMap, List[String])] = remainingArgs match {
    case "--path" :: jarPath :: Nil     if isJarFile(jarPath) => Some(map ++ Map("jarPath" -> jarPath), List())
    case "--path" :: jarPath :: tail    if isJarFile(jarPath) => Some(map ++ Map("jarPath" -> jarPath), tail)
    case tail => None
  }
}

class InstanceNameSwitch extends CmdLineSwitch {
  override def check(map: OptionMap, remainingArgs: List[String]): Option[(OptionMap, List[String])] = remainingArgs match {
    case "--instancename" :: instanceName :: Nil      => Some(map ++ Map("instanceName" -> instanceName), List())
    case "--instancename" :: instanceName :: tail     => Some(map ++ Map("instanceName" -> instanceName), tail)
    case tail => None
  }
}

class ScalaFileSwitch extends CmdLineSwitch {
  private def isScalaFile(s : String): Boolean = (s.trim.endsWith(".scala"))
  override def check(map: OptionMap, remainingArgs: List[String]): Option[(OptionMap, List[String])] = remainingArgs match {
    case "--path" :: scriptPath :: Nil      if isScalaFile(scriptPath) => Some(map ++ Map("scriptPath" -> scriptPath), List())
    case "--path" :: scriptPath :: tail     if isScalaFile(scriptPath) => Some(map ++ Map("scriptPath" -> scriptPath), tail)
    case tail => None
  }
}

class LogToConsoleSwtich extends CmdLineSwitch {
  override def check(map: OptionMap, remainingArgs: List[String]): Option[(OptionMap, List[String])] = remainingArgs match {
    case "--console" :: Nil      => Some(map ++ Map("logToConsole" -> true), List())
    case "--console" :: tail     => Some(map ++ Map("logToConsole" -> true), tail)
    case tail => None
  }
}

*/