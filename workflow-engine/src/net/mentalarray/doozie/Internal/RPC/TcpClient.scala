package net.mentalarray.doozie.Internal.RPC

import java.net.{InetAddress, Socket}

/**
 * Created by kdivincenzo on 2/6/15.
 */
class TcpClient(address: String, port: Int) extends Client with TcpDetails {

  def this(port: Int) = this(null, port)

  private val _address = InetAddress.getByName(address)
  private var _channel : TcpChannel = null
  private lazy val _socket: Socket = new Socket(_address, port)

  /**
   * Checks if the channel is still connected.
   * @return true if connected, false otherwise.
   */
  override def isConnected: Boolean = _socket.isConnected

  /**
   * Closes the client connection.
   */
  override def close(): Unit = {
    // Stop the channel
    _channel ?! { _.stopReceiving }

    // Close the socket
    _socket ?! { _.close }
  }

  /**
   * The IpcChannel that the client is connected to.
   * @return The actual IpcChannel channel.
   */
  override def channel : IpcChannel = {
    // Open the socket
    _socket

    // Create the channel
    _channel = new TcpChannel(_socket)

    // Return the channel
    _channel
  }

}