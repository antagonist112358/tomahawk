package net.mentalarray.doozie.Internal

/**
 * Created by kdivincenzo on 2/6/15.
 */
trait Client {

  /**
   * Checks if the channel is still connected.
   * @return true if connected, false otherwise.
   */
  def isConnected : Boolean

  /**
   * Closes the client connection.
   */
  def close() : Unit

  /**
   * The IpcChannel that the client is connected to.
   * @return The actual IpcChannel channel.
   */
  def channel : IpcChannel
}
