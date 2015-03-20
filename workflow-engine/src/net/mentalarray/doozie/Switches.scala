package net.mentalarray.doozie

/**
 * Created by kdivincenzo on 10/20/14.
 */
object Switches {

  private val cmdLineSwitches : List[CmdLineSwitch] = List(

    // JarFile switch
    new SingleArgumentSwitch("path", "jarPath") {
      override def checkArgument(arg: String) = (arg.trim.endsWith(".jar"))
    },

    // InstanceName switch
    new SingleArgumentSwitch("instancename", "instanceName") {  },

    // ScriptFile switch
    new SingleArgumentSwitch("path", "scriptPath") {
      override def checkArgument(arg: String) = (arg.trim.endsWith(".scala"))
    },

    // Testing switch
    new NoArgumentSwitch("test", "testingOnly") { },

    // Console logging
    new NoArgumentSwitch("console", "logToConsole") { }
  )

  def allSwitches : List[CmdLineSwitch] = cmdLineSwitches
}
