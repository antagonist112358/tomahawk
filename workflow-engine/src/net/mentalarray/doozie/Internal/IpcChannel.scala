package net.mentalarray.doozie.Internal

import net.mentalarray.doozie.Internal.RPC.IpcMessage

/**
 * Created by kdivincenzo on 2/5/15.
 */
trait IpcChannel {

  /**
   * The TCP address of this channel's endpoint.
   * @return The address
   */
  def address : String

  /**
   * The TCP port of this channel's endpoint.
   * @return The TCP port number.
   */
  def port : Int

  /**
   * Sends a message across the channel
   * @param msg The message to send
   */
  def sendMessage(msg: IpcMessage)

  /**
   * Called when a new message is received on the channel
   * @param handler The method which will be invoked when a new message is received
   */
  /* Setter */ def onMessageReceived_=(handler: IpcMessage => Unit)
  /* Getter */ def onMessageReceived : IpcMessage => Unit

  /**
   * Can be used to assign an exception handler to exception produced in the receiver thread
   * @param exHandler The function which will handle the produced exceptions.
   */
  /* Setter */ def onReceiverException_=(exHandler: Exception => Unit)
  /* Getter */ def onReceiverException : Exception => Unit

  /**
   * Can be used to assign a disconnect handler which will be raised if the channel's endpoint becomes disconnected.
   * @param disconnectHandler The function which will handle the produced exceptions.
   */
  /* Setter */ def onEndpointDisconnected_=(disconnectHandler: IpcChannel => Unit)
  /* Getter */ def onEndpointDisconnected : IpcChannel => Unit

  /**
   * Handles closing of the listener and the socket.
   * @note This method does not raise the 'onEndpointDisconnect' event.
   */
  def close() : Unit
}
