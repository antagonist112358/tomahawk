package net.mentalarray.doozie.Internal

/**
 * Created by kdivincenzo on 2/6/15.
 */
trait Server {

  /**
   * The port that the server is listening on.
   * @return The TCP port
   */
  def port: Int

  /**
   * The delegate type of the OnClientConnected event callback (handler).
   */
  type OnClientConnected = IpcChannel => Unit

  /**
   * Starts the server listening for client connections.
   */
  def startListening() : Unit

  /**
   * Initiates the shutdown of the server.
   */
  def shutdown() : Unit

  /**
   * Gets the current OnClientConnection callback.
   */
  def onClientConnected : OnClientConnected

  /**
   * Sets the current OnClientConnected callback.
   * @param value The callback to invoke when a new client is connected.
   */
  def onClientConnected_=(value: OnClientConnected) : Unit

}
