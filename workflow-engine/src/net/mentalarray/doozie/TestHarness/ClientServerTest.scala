package net.mentalarray.doozie.TestHarness

/**
 * Created by kdivincenzo on 2/17/15.
 */

final object ClientServerTest {

  final val port = 17374

  /**
   * Entry Point
   * @param args Cmdline Arguments Array
   */
  def main(args: Array[String]) {

    // Start the TCP server
    val server = new TcpServer(port)

    // Assign the onClientConnected handler
    server.onClientConnected = { client =>
      // Notification
      println("Client Connected.")
      // Handle client messages
      client.onMessageReceived = msg => msg match {
        case isOnline : IsOnline =>
          // Notification
          println("Server: IsOnline message received from client.")
          // Send an IsOnline message back
          client sendMessage IsOnline()
        case _ => throw new Exception("Unexpected client message received.")
      }
    }

    println("Starting server...")
    server.startListening()

    Thread.sleep(500)
    println("Server Started - Launching client...")

    // Start the timeout process
    Timer(5000, false) {
      println("Client response never received...Terminating forcefully.")
      sys exit 1
    }

    // Use JavaProcess to launch client in a new process
    val client = JavaProcess runAndWait(
      // Properties
      Seq(
        // Application directory
        "app.currentDirectory" -> Application.applicationDirectory
      ),
      // EntryPoint Class
      ClientStub.getClass,
      // Tcp port to connect on
      port.toString
    )

    try {

      // If the client exited with code 0, success
      if (client == 0) {
        println("Test was successful - Success!")
        sys exit 0
      } else {
        println("Test failed due to error.")
        sys exit 1
      }

    } finally {
      // Shutdown the server
      server shutdown
    }
  }

}

final object ClientStub {

  // For logging
  private val Logger = new {} with Logging {}

  /**
   * Client (TCPClient) Entry Point
   * @param args Cmdline Arguments Array
   */
  def main(args: Array[String]): Unit = {

    // Startup message
    Logger.Log.info("Client Starting...")

    // Outer Try/Catch
    try {

      // Parse the port
      val port = args(0).toInt

      // Create the client
      val client = new TcpClient(null, port)

      // Get the IpcChannel
      val channel = client.channel

      // Attach a message received handler
      channel.onMessageReceived = msg => msg match {
        case isOnline: Messages.IsOnline =>
          Logger.Log.info("Reply from server received.")
          Logger.Log.info("Shutting down client")
          sys.exit(0)
        case _ => {
          throw new Exception("Unexpected reply message.")
        }
      }

      // Send a "IsOnline" message across the channel - we expect an IsOnline reply from the server
      channel sendMessage Messages.IsOnline()

      // Specify that we are waiting on a reply
      Logger.Log.info("Waiting on a reply from the server...")

      // Waits 5 seconds then times out with an error
      Timer(5000, false) {
        Logger.Log.info("No reply from server received... shutting down client.")
        sys.exit(1)
      }

    } catch {
      case e: Throwable => Logger.Log.info(s"Client Exception:$e")
    }
  }

}