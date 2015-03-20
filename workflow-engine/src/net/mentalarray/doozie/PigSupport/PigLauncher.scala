package net.mentalarray.doozie.PigSupport

import java.util.Properties

import org.apache.pig.ExecType
import org.apache.pig.backend.hadoop.datastorage.ConfigurationUtil
import org.apache.pig.impl.PigContext
import org.apache.pig.impl.util.PropertiesUtil
import org.apache.pig.scripting.Pig

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
 * Created by kdivincenzo on 1/9/15.
 */

final object PigBootstrap {
  // UUID type alias
  type UUID = java.util.UUID

  // Logging
  private val Logger = new Log4JLogger(PigBootstrap.getClass.getName.replace("$", ""))

  // Entry point for the PigLauncher process, remember to enter "gracefully"
  def main(implicit args: Array[String]): Unit = {

    val runtimeName = java.lang.management.ManagementFactory.getRuntimeMXBean.getName
    Logger.debug(s"PigLauncher Starting on $runtimeName")

    try {
      // Boot the application (needs to be the very first line in this class)
      Application.boot

      // Set the temporary directory for this java process
      val tmpDir = Application.temporaryManager.setTemporaryDirectory
      Logger.debug(s"Temporary directory for this job set to: $tmpDir")

      // The two things we need from the command line are the port number to connect back to, and the UUID of this job
      // They are expected in the format "{port number} {uuid}"
      val portNumber = TryParseArgs[Int](0) { s => s.toInt}
      val uuid = TryParseProperty[UUID]("pigLauncher.UUID") {
        s =>
          Logger.debug(s"pigLauncher.UUID=$s")
          java.util.UUID.fromString(s)
      }
      val uuidStr = uuid.toString

      // Log the uuid to the console/logfile/whatever
      Logger.info(s"Running: PigLauncher Stub - Id: $uuidStr")
      Logger.info("==========================================================================")
      Logger.info(s"Host process listening at localhost:$portNumber")

      // Connect to the PigServer on the specified port
      val channel = openConnection(portNumber)

      // Create a connection manager
      val clientManager = new PigServerConnectionManager(channel)

      // For the PigServerConnectionManager
      try {

        // Notify that we are online, and request the PigTask from the server
        channel.sendMessage(StubOnline(uuid))

        // The PigTask is a future
        val futureWork = clientManager futurePigTask

        // When we get a task
        futureWork await match {
          // Work received
          case Success(task) => {
            Logger.info("PigTask received from the PigServer.")
            Logger.debug("Starting GruntParser and running PigTask...")

            // The PigRunner which will execute the PigTask
            val runner = new PigRunner(task)

            // Execute the runner and await the result
            runner.execute await match {

              // PigTask executed (might have failed, but no exceptions while trying to run it)
              case Success(result) => {
                val resText = if (result) "Passed" else "Failed"
                Logger.info(s"PigTask execution status: $resText")
                // Send the server the result
                channel sendMessage PigResult(result)
              }

              // Failure - Exception while trying to run the PigTask
              case Failure(ex) => {
                Logger.warn("PigTask execution resulted in unhandled exception.", ex)
                // Send the server the exception
                channel sendMessage RemoteException(ex.asInstanceOf[Exception])
              }

            }
          }
          // Work not received or client timed out
          case Failure(e) => {
            Logger.error("Encountered an error while trying to receive work from the PigServer.", e)
          }
        }

      } finally {
        Logger.debug("Shutting down PigServerConnectionManager...")
        // Shutdown the clientManager thread and close the channel
        clientManager shutdown
      }

      Logger.info("PigStub shutting down gracefully.")

    } catch {
      case ex : Throwable =>
        Logger.error(ex)
        throw ex
    } finally {
      val tempDir = Application.temporaryManager.get
      Logger.debug(s"Clearing Temp Directory: $tempDir")
      Application.temporaryManager.clearTemporaryDirectory
      Application.temporaryManager.deleteTemporaryDirectory
      // Return 0
      System.exit(0)
    }
  }

  // Opens a connection to the PigServer and supplies the channel
  private def openConnection(port: Int) : IpcChannel = {
    Logger debug s"Connecting to PigServer on port: ${port}"
    // Open the Tcp connection to the PigServer
    val _clientSocket = new TcpClient(port)
    // Get the IPC channel
    _clientSocket.channel
  }

  def TryParseArgs[T](position: Int)(jamesBrown: String => T)(implicit args: Array[String]) : T = {
      if (position >= args.size)
        throw new ArrayIndexOutOfBoundsException("Not enough arguments.")

      jamesBrown(args(position))
  }
  
  def TryParseProperty[T](propertyKey : String)(jamesBrown : String => T) : T = {
    val prop = System.getProperty(propertyKey)
    
    if (prop == null || prop.isEmpty)
      throw new Exception("Missing property value for key: " + propertyKey)

    jamesBrown(prop)
  }
}

class PigServerConnectionManager(channel: IpcChannel) extends Logging with TimeTracking {

  private val _pigTaskPromise = Promise[PigTask]

  // Configure the listener for this channel
  channel.onMessageReceived = (msg) => msg match {

    // IsOnline received from server
    case IsOnline() =>
      // Update
      update()

    // CheckOnline received from server
    case CheckOnline() =>
      // Respond in kind
      channel sendMessage IsOnline()

    // PigTask information received from the server
    case PigTaskData(task) =>
      Log debug s"PigTask data received from PigServer (XML: ${task.toXml.toString})"
      // Fulfill the pigTask promise
      _pigTaskPromise success task

    // RemoteException received from the server
    case RemoteException(e) =>
      Log error("PigServer could not deliver PigTask information successfully due to an error.")
      // Fulfill the pigTask promise
      _pigTaskPromise failure e

    // Any other messages
    case msg : IpcMessage =>
      Log error s"Unexpected RPC from PigServer to PigClient: ${msg.toString}"
  }

  // Schedule periodic "CheckOnline" messages to be sent
  private val heartThumper = Timer(3000, true) {
    channel sendMessage( CheckOnline() )
  }

  // Allows the stub to await the PigTask from the server
  def futurePigTask : Future[PigTask] = _pigTaskPromise future

  def shutdown : Unit = {
    // Stop the heartbeat
    heartThumper stop

    // Close the socket
    channel close
  }
}

/**
 * Responsible for invoking Pig via the GruntParser
 * @param task The PigTask to run
 */
private class PigRunner(task: PigTask) {

  // The Pig wrapper class
  private val _pigServerProxy = buildPigJob(task)

  /* Public Methods */

  /**
   * Must be called when the runner is no longer needed!
   */

  /**
   * Executes the task using Pig.
   * @return A future[bool] which is the outcome (true = pass, false = fail) of the Pig job.
   *
   */
  def execute : Future[Boolean] = {

    val p = Promise[Boolean]

    // Careful, you can totally screw yourself with access to objects within this context
    DataflowThread {

      // Close over p
      val promise = p

      try {
        // Run pig
        val pigRes = _pigServerProxy.exec()

        // Convert results
        val boolRes = calcResults(pigRes)

        // Success
        promise.success(boolRes)
      } catch {
        case e: Exception => promise.failure(e)
      }

    }

    // Return the Future[Boolean]
    p.future
  }

  def cleanup(): Unit = {
    _pigServerProxy.shutdown()
  }

  /* Private Methods */

  /**
   * Creates an instance of PigInBlanket class, which is a wrapper for some Pig classes.
   * @param taskDef The PigTask to inject into the GruntParser associated with the PigInBlanket instance.
   * @return The PigInBlanket instance which can be used to run the provided PigTask.
   */
  private def buildPigJob(taskDef: PigTask) = {
    // Create the configuration
    val config = Application.hadoopConfiguration

    // Setup properties
    val props = new Properties
    PropertiesUtil.loadDefaultProperties(props)
    props.putAll(taskDef.serverProperties)
    props.putAll(ConfigurationUtil.toProperties(config))

    // Create a pigContext
    val pigContext = new PigContext(ExecType.MAPREDUCE, props)

    // Register the Jars
    for (jar <- taskDef.jarPaths) {
      Pig.registerJar(jar)
    }

    // Grunt really loud!
    val eggsAndBacon = new PigInBlanket(taskDef.script, pigContext)

    // Return the delicious food
    eggsAndBacon
  }

  // Used to compute the output of the tasks run
  private final def calcResults(results: Array[Int]) : Boolean = {
    if (results(1) > 0 ) return false
    return true
  }
}
