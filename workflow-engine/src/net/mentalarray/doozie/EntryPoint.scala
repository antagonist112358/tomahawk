package net.mentalarray.doozie


final object EntryPoint {

  // Boot the application (needs to be the very first line in this class)
  Application.boot

  private val Green = Console.GREEN
  private val Reset = Console.RESET
  private val title = Application.title
  private val version = Application.version

  // Entry point for the application

  def main(args: Array[String]) {
    println(s"\nRunning: $Green$title$Reset - $version")
    println("==========================================================================")

    if (!ArgCheck.setArgs(args)) {
      sys.exit()
    }

    val tmpDir = Application.temporaryManager.setTemporaryDirectory
    println(s"Temporary directory for this job set to: $tmpDir")
    println("Starting the PigServer service...")
    PigServer start()
    println(s"PigServer service started on port: ${PigServer.port}")
    println(String.format("Loading workflow from %s...", ArgCheck.fileName))
    val workflow = WorkflowReader(WorkflowMode(ArgCheck.mode), ArgCheck.fileName, ArgCheck.instanceName)
    println(String.format("Loaded workflow from %s...", ArgCheck.fileName))
    val wf = workflow.build
    println(String.format("Workflow built from %s...", ArgCheck.fileName))

    println("==========================================================================\n")

    // Enable console output
    //Log4JLogger.enableConsoleOutput
    // Set the logging level to info

    wf.execute
    println("\n==========================================================================")
    println("Executing cleanup phase")
    println("==========================================================================")
    PigServer stop()
    println("Deleting the temporary directory for this job...")
    Application.temporaryManager.deleteTemporaryDirectory
    println("\nThanks for using DataFlow!")
    System.exit(0)
  }


  def testMain(args: Array[String]) {
    val options = ApplicationOptions.parseArgs(args.toList)
    println(options)
  }

}
