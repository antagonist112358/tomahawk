package net.mentalarray.doozie.Internal.RPC

import java.io.IOException
import java.net.Socket

import net.mentalarray.doozie.Internal.{IpcChannel, ThreadPool}
import net.mentalarray.doozie.Logging

/**
 * Created by kdivincenzo on 2/5/15.
 */
sealed class TcpChannel(socket: Socket, onReceive: IpcMessage => Unit = null) extends IpcChannel with Logging {

  /* This has the simplest dumb wire-format ever:

    Header: Length: UInt32 (4 bytes)
    Body: ByteArray - {Length bytes}

   */

  /**
   * Constructor which does not require a receiver callback.
   * @param socket The Tcp/Ip socket which the channel will utilize.
   */
  def this(socket: Socket) = {
    this(socket, null)
  }

  // The receiver and receive exception handler
  private var _receiver: IpcMessage => Unit = onReceive
  private var _recvExHandler: Exception => Unit = null
  private var _onDisconnect: IpcChannel => Unit = null

  // The receiver and receive ex handler locks
  private[this] val exLock = new Object()
  private[this] val recvLock = new Object()

  // Allows for retrieving the currently assigned handlers for different event types
  def onReceiverException = exLock.synchronized { _recvExHandler }
  def onMessageReceived = recvLock.synchronized { _receiver }
  def onEndpointDisconnected = exLock.synchronized { _onDisconnect}

  // Handles assignments from the outside world to the dispatchers
  def onReceiverException_=     (exHandler: Exception => Unit)          = exLock.synchronized   { _recvExHandler = exHandler }
  def onMessageReceived_=       (handler: IpcMessage => Unit)           = recvLock.synchronized { _receiver = handler }
  def onEndpointDisconnected_=  (disconnectHandler: IpcChannel => Unit) = exLock.synchronized   { _onDisconnect = disconnectHandler }

  // These are used by the receiver thread to dispatch messages or de-serialization exceptions
  private def raise(e: Exception) = exLock.synchronized { if (_recvExHandler != null) _recvExHandler(e) }
  private def raiseDisconnect() = exLock.synchronized { if (_onDisconnect != null) _onDisconnect(this) }
  private def onMessage(msg: IpcMessage) = recvLock.synchronized { if (_receiver != null) _receiver(msg) }

  /*** Properties for the channel's endpoint. ***/
  def address : String = socket.getInetAddress.toString
  def port : Int = socket.getPort

  // Setup output stream
  private val output = socket.getOutputStream

  // Setup the receiver thread and start it
  private val receiverThread = DataflowThread {

    // Log
    Log debug "Starting TcpChannel receiver thread."

    // Setup the input stream
    val input = socket.getInputStream

    // Abort on any IOExceptions
    try {

      // Wait for a listener to be assigned
      while (_receiver == null) {
        // Don't waste cycles
        Thread sleep 100
      }

      // Setup the reused Int32 buffer
      val int32Buffer = new Array[Byte](4)

      // While the socket is still connected
      while (socket.isConnected) {

        // Try to read 4 bytes (for int)
        val int32Read = input.read(int32Buffer, 0, 4)

        // Did the client disconnect?
        if (int32Read == -1)
          throw new IOException("Peer disconnected.")
        //  Did we read a whole int32?
        else if (int32Read < 4) {
          // Wait for and read the next 'n' bytes
          input.read(int32Buffer, int32Read, 4 - int32Read)
        }

        // Read int32 is the length of the message data (in bytes)
        val length = Serializer.readInt(int32Buffer, 0)

        // If the length is 0 or less, ignore
        if (length > 0) {

          // Buffer for the message contents
          // Note: This is a stupid/lazy idea, if you don't understand why, don't worry about it
          val body = new Array[Byte](length)

          // Read the buffer
          val readLength = input.read(body, 0, length)

          // Throw if client disconnected, else assert that read length equal expected length
          if (readLength == -1)
            throw new IOException("Peer disconnected.")
          else
            assert(readLength == length)

          // Message variable (outside the try/catch scope)
          var msg: IpcMessage = null

          try {
            // Deserialize the message
            msg = Messages.deserialize(body)
          } catch {
            case e: Exception =>
              Log error("Encountered de-serialization exception: ", e)
              raise(e)
          }

          // Since we don't want to raise an exception here due to shitty receive handlers,
          // and we want to remain responsive to incoming messages, lets dispatch the message on the thread-pool
          if (msg != null) ThreadPool fireAndForget onMessage(msg)

        }
        else
          Log debug s"Ignoring message of length $length"
      }

      // If the client disconnected on us, report that
      if (!socket.isConnected) { raiseDisconnect() }

    } catch {
      case ioEx: IOException =>
        Log debug "Lost connection to IpcChannel peer."
        raiseDisconnect()
      case e: Throwable => Log error ( "Unhandled IO error in TcpChannel receive thread: ", e )
    }
  }

  // Handle sending
  def sendMessage(msg: IpcMessage): Unit = {
    // Get the message bytes
    val msgBytes = msg.toBytes
    // Write message size
    output write Serializer.getBytes(msgBytes.size)
    // Write the message contents
    output write msg.toBytes
    // Flush the stream
    output flush()
  }

  // Handle closing the channel
  def stopReceiving() {
    receiverThread stop
  }

  // Handle closing the channel
  def close() {
    socket close()
    stopReceiving()
  }
}
