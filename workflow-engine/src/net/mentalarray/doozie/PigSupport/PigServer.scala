package net.mentalarray.doozie.PigSupport

import java.util.UUID

import scala.collection.mutable
import scala.concurrent.Future

/**
 * Created by kdivincenzo on 2/18/15.
 */
class PigServer private() extends Service with Logging with PigClientTracker {

  final private val loggingConfigPath = "file:" + Path.combine(Application.applicationDirectory, "conf/pigstub4j.xml")

  // Used to communicate with pig-stub processes which actually launch the Grunt shell to execute pig jobs.
  private val server : Server = TcpServer.makeServer
  private lazy val serverPort = server.port

  // The work tracker
  private val workTracker = new PigWorkTracker

  // We use a map to both store all the connected / known clients, but also to map those clients to a particular UUID
  private val clientsStore = mutable.ArrayBuffer.empty[PigClient]

  // Atomic access to the workStore
  protected val clientsList = AtomicWrapper(clientsStore)

  // Attach the handlers
  server.onClientConnected = handleClientConnected


  /****************************/
  /*** Client / API Methods ***/
  /****************************/
  def assignWork(task: PigTask) : Future[Boolean] = {
    // Start tracking the work
    val (id, future) = workTracker registerWork task
    Log debug s"PigTask registered under WorkID: $id"
    // Launch the child process
    startPigStubProcess(serverPort, id)
    // Provide the client with the work's future
    future
  }

  /****************************/
  /** Service Implementation **/
  /****************************/
  // Service name for this service
  override def name: String = "PigService"

  /**
   * Stops this particular service.
   */
  def stop(): Unit = {
    // Stop the client timeout tracker
    Log debug "Stopping tracker..."
    stopTracker()
    // Stop the listener
    Log debug "Shutting down listener..."
    server.shutdown()
    // Close every client
    Log debug "Closing lingering client connections..."
    clientsList atomicAction(_.map { x => x.close })
    Log info "PigServer has stopped."
  }

  /**
   * Starts this particular service.
   */
  def start(): Unit = {
    // Start the listener
    server.startListening()
    // Start the client timeout tracker
    startTracker()
  }

  // Launches the stub (child) process which connects back to the PigServer to get the PigTask details
  private def startPigStubProcess(port: Int, id: UUID) : Unit = {

    Log info s"Launching child process to execute PigTask..."

    // Create a PigLauncher in a new process
    JavaProcess runAsync (
      // Properties
      Seq(
        // Application directory
        "app.currentDirectory" -> Application.applicationDirectory,
        // Log4J configuration
        "log4j.configuration" -> loggingConfigPath,
        // UUID for this PigLauncher
        "pigLauncher.UUID" -> id.toString
      ),
      // The Bootstrap class
      PigBootstrap.getClass,
      // IPC port
      port.toString
    )

    Log debug "Started new JavaProcess for PigTask successfully."
  }

  /**************************/
  /*** Ipc Implementation ***/
  /**************************/

  /**
   * Handles clients connecting to the pig server.
   * @param channel The IpcChannel for the client which connected for this event.
   * @note This method must be thread safe as it will be called by different threads.
   */
  private def handleClientConnected(channel: IpcChannel): Unit = {
    // Register the client in the client's list
    val client = new PigClient(channel)
    clientsList atomicAction { _ += client }

    // Log
    Log info s"Client connected from ${client.getClientAddress}"
    Log debug "Awaiting PigTask request message from client."

    // Assign the message received handler, which we will wrap the clientAddress for details
    client onMessageReceived = (msg: IpcMessage, c: PigClient) => {
      // Try to properly handle the message
      try { handleReceivedMessage(msg, c) }
      // On Error, reply with the client address
      catch { case e: Exception => Log error s"Received malformed data from client ${client.getClientAddress}" }
    }

    // Assign the client disconnected handler
    client onClientDisconnect = (c: PigClient) => handleClientDisconnect(c)

  }

  /**
   * Handles IPC messages received from the pig client processes
   * @param msg The IpcMessage received from the client.
   * @param client The IpcChannel (client) which dispatched the message to the PigServer
   * @note This method must be thread safe as it will be called by different threads
   */
  private def handleReceivedMessage(msg: IpcMessage, client: PigClient): Unit = msg match {

    // StubOnline - Client sends this message when they first come online, which also serves as the PigTaskData request.
    case StubOnline(uuid) =>
      Log debug s"PigTask request message received from client: ${client.getClientAddress}"
      // Get the work for the requested UUID
      val workItem = workTracker tryGetWorkForID uuid
      // Make sure we have work matching the requested ID
      val hasWorkForUUID = workItem match {case Some(_) => true; case None => false}
      // If we have work, send that work to the client
      if (hasWorkForUUID) {
        Log debug s"PigWorkItem found for UUID: $uuid"
        // Assign the UUID to the client
        client.workId = uuid
        // Build the reply message
        val pigTaskMessage = PigTaskData(workItem.get)
        // Send a reply
        Log debug s"Sending PigTask for UUID: $uuid to client: ${client.getClientAddress}"
        client sendMessage pigTaskMessage
        Log info "PigTask sent to PigStub client."
      } else {
        // Send a remoteException (not found)
        Log error s"No PigTasks found for UUID: $uuid"
        val notFoundEx = new Exception(s"No PigTasks found for UUID: $uuid")
        client sendMessage RemoteException(notFoundEx)
      }

    // IsOnline - Client sends this in response to CheckOnline message
    case IsOnline() =>
      // Update the client with the latest heartbeat
      client.update()

    // CheckOnline - Send from the client to make sure the server is still responsive / online
    case CheckOnline() =>
      // Send a reply
      client sendMessage IsOnline()

    // PigTaskData - Should never be received from the client
    case PigTaskData(_) =>
      // Log
      Log error s"Client ${client.getClientAddress} erroneously sent a 'PigTaskData' message. This should not happen."
      // RemoteException
      client sendMessage RemoteException(new Exception("Client should never send a PigTaskData message."))

    // PigResult - Normal completion of the execution of a PigTask
    case PigResult(result) =>
      // Log
      Log info s"Client ${client.getClientAddress} completed PigTask and has reported results."
      // Get the UUID of the WorkItem this client was working on
      val uuid = client.workId
      // Finish the work
      workTracker workCompleted(uuid, Left(result))

    // RemoteException - Received from the client in the event an unhandled exception occurred
    case RemoteException(e) =>
      // Log
      Log warn(s"Client ${client.getClientAddress} encountered an error while trying to execute PigTask.", e)
      // Get the UUID of the WorkItem this client was working on
      val uuid = client.workId
      // Finish the work
      workTracker workCompleted(uuid, Right(e))

    // Some unknown message we were not expecting to receive
    case _ => throw new Exception("Unknown client message.")
  }

  /**
   * Handles clients disconnecting to the pig server.
   * @param client The IpcChannel for the client which disconnected.
   * @note This method must be thread safe as it will be called by different threads
   */
  private def handleClientDisconnect(client: PigClient): Unit = {
    Log info s"PigClient ${client.getClientAddress} disconnected."
    // If the client wasn't finished processing the work, this is an error condition
    if (client.state != PigClientState.Finished) {
      Log warn s"PigStub for task ${client.workId} failed to process work successfully."
      workTracker workCompleted(client.workId, Right(new Exception("PigStub failed to process WorkItem successfully.")))
    }

    // Remove the client from the list of known clients
    clientsList atomicAction { m => m remove (m indexOf client) }
  }

}

object PigServer extends Service {

  // The singleton instance
  private lazy val _singleton = new PigServer

  /**
   * Allows clients to assign work to the PigServer service, which will spawn additional processes as necessary to complete that work.
   * @param task The PigTask to execute using the PigServer.
   * @return A Future[Boolean] which corresponds to the completion (or failure) of the supplied PigTask.
   */
  def assignWork(task: PigTask) : Future[Boolean] = _singleton.assignWork(task)

  /**
   * The service name for this service.
   * @return The name
   */
  override def name: String = _singleton name

  /**
   * Stops this particular service.
   */
  override def stop(): Unit = _singleton stop

  /**
   * Starts this particular service.
   */
  override def start(): Unit = _singleton start

  /**
   * The port that the PigServer is listening for client connections on.
   * @return The TCP port of the PigServer.
   */
  def port : Int = _singleton.serverPort

}