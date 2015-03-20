package net.mentalarray.doozie

/**
 * Created by bgilcrease on 9/25/14.
 */
object ArgCheck {

  private val jarName = Application.title + "-" + Application.version + ".jar"

  val usage = s"""
       Usage: java jar $jarName [--path path] [--instancename instanceOf[WorkflowBuilder] ]

       Runs with either a scala file that returns a WorkflowBuilder instance or a jar containing a precompiled WorkflowBuilder

       --path         : The location of the scala file or jar
       --instancename : The name of the instance of WorkflowBuilder contained in the jar file
       --console      : Specifies that logging output should be sent to the console.


       Example:
       java jar $jarName --path ~/FabTracking.scala
       java jar $jarName --path ~/FabTrackingScalding.jar --instancename net.mentalarray.doozie.examples.fabtracking
              """

  private var argMap: Map[String,String] = null
  private var workflowMode: String = null
  private var _supressUsage = false

  private def getRequiredArg(arg: String): String = {
    if ( argMap == null || !argMap.contains(arg) ) {
      throw new Exception("Argument not found in Args.")
    }

    // Return the value
    argMap(arg)
  }

  private def printUsage : Unit = {
    if (!_supressUsage)
      println(usage)
  }

  def suppressUsage {
    _supressUsage = true
  }

  def fileName: String = {
    getRequiredArg("path")
  }

  def instanceName: Option[String] = {
    argMap.get("instancename")
  }

  def mode = {
    if( workflowMode != null ) {
      workflowMode
    } else {
      printUsage
      sys.exit()
    }
  }

  def setArgs(args: Array[String]): Boolean =  {
    //must make lots of code... nom nom nom nom

    // If there were not at least 2 args provided
    if (args.length < 2) {
      printUsage
      false
    }

    try {

      // The filename
      val fileName = args(1).trim

      // Check for '.scala' file
      if (fileName.toLowerCase.endsWith(".scala") && args.length == 2) {
        argMap = Map(args(0).replace("--", "").trim -> fileName)
        workflowMode = "scala"
        return true
      } else if (fileName.toLowerCase.endsWith(".jar") && args.length == 4) {
        argMap = Map(args(0).replace("--","").trim -> fileName, args(2).replace("--","") -> args(3))
        workflowMode = "jar"
        return true
      }

    } catch {   // On exception, assume (safely) invalid arguments
      case _: Throwable => { printUsage; false}
    }

    // No exception, but not match found either
    printUsage
    false

  }


}
