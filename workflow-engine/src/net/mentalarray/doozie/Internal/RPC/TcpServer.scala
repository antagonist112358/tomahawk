package net.mentalarray.doozie.Internal.RPC

import java.net.{ServerSocket, Socket}

import net.mentalarray.doozie.Internal.DataflowThread

/**
 * Created by kdivincenzo on 2/9/15.
 */
// Todo: Add constructor or property methods which support automatically assigning the client's TcpChannel's OnMessageReceived callback.
class TcpServer(_port: Int) extends Server with TcpDetails {

  // The ServerSocket
  private val serverSocket = createServerSocket(_port)

  // The Listener thread
  private var listener : DataflowThread = null

  /**
   * The onClientConnected handler and the lock
   */
  private[this] var _onClientConnected : OnClientConnected = null
  private[this] val occLock = new Object()

  /**
   * The port that the server is listening on.
   * @return The TCP port
   */
  def port: Int = _port

  /**
   * Gets the current OnClientConnection callback.
   */
  def onClientConnected : OnClientConnected = occLock.synchronized { _onClientConnected }

  /**
   * Sets the current OnClientConnected callback.
   * @param value The callback to invoke when a new client is connected.
   */
  def onClientConnected_=(value: OnClientConnected) = occLock.synchronized { _onClientConnected = value }

  /**
   * This is used to handle client connection events.
   * @param clientSocket The socket which is the client connection.
   */
  private def handleClientConnected(clientSocket : Socket) = occLock.synchronized {
    if (_onClientConnected != null) {
      // Create a new channel
      val channel = new TcpChannel(clientSocket)
      // Pass the channel to the handler
      _onClientConnected(channel)
    }
  }

  /**
   * Starts the server listening for client connections.
   */
  def startListening() {

    // Create the listener thread
    listener = DataflowThread {

      while (serverSocket.isBound) {

        // Wait for a connection
        val clientSock = serverSocket.accept()

        // Notify of the connection
        handleClientConnected(clientSock)
      }

    }

  }

  /**
   * Initiates the shutdown of the server.
   */
  def shutdown() {
    // Unbind and close the server socket
    serverSocket close()
    // Stop the listener
    listener stop()
  }

  /* Utility / helper methods */
  private final def createServerSocket(sPort: Int) : ServerSocket = new ServerSocket(sPort, 1, loopback)

}


object TcpServer {

  private val portMin = 2048
  private val portMax = 65535

  def makeServer : TcpServer = {

    var outServer : TcpServer = null
    val random = new java.util.Random
    def nextPort = random.nextInt(portMax - portMin + 1) + portMin

    while (outServer == null) {
      try { outServer = new TcpServer(nextPort) }
      catch { case _ : Throwable => {} }
    }

    outServer
  }

}