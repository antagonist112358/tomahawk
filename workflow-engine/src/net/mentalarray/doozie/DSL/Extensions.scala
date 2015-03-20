package net.mentalarray.doozie.DSL

import net.mentalarray.doozie.Tasks.{BashTask, HdfsTask}
import net.mentalarray.doozie.ValueProxy.TaskResult
import net.mentalarray.doozie.{Param, Switch}

/**
 * Created by kdivincenzo on 10/2/14.
 */
object Extensions {

  implicit def tupleToParam(tuple: (String, String)) : Param = new Param(tuple._1, tuple._2)
  implicit def stringToSwitch(s: String) : Switch = new Switch(s)

  implicit class StringToBashTask(s: String) {
    def shellCmd : BashTask = {
      val shellCmd = new BashTask("ShellCommand: '%s'" format s)
      shellCmd.setCommand(s)
      shellCmd
    }
  }

  implicit class StringToHDFSTask(s: String) {
    def hdfsCmd : HdfsTask = {
      val hdfsCmd = new HdfsTask("HdfsCommand: '%s'" format s)
      hdfsCmd.setCommand(s)
      hdfsCmd
    }
  }

  implicit class DeferredTextStringBehavior(s: TaskResult[String]) {
    def format(args : Any*): TaskResult[String] = s.mutate {
      _ format(args)
    }
  }

  implicit def deferredTextToCommand(cmdText : TaskResult[String]) : AbstractCommand = new Command(cmdText)
}

