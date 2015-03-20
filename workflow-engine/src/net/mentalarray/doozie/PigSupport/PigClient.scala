package net.mentalarray.doozie.PigSupport

import java.util.UUID

import net.mentalarray.doozie.Internal.RPC.IpcMessage
import net.mentalarray.doozie.Internal.{IpcChannel, TimeTracking}

/**
 * Created by kdivincenzo on 2/18/15.
 */
protected[PigSupport] class PigClient(_channel: IpcChannel) extends TimeTracking {

  type PigClientMessageReceivedHandler = (IpcMessage, PigClient) => Unit
  type PigClientEndpointDisconnectHandler = (PigClient) => Unit

  // Private variable for correlating the PigWorkItem to this client
  private var _workId: Option[UUID] = None

  // Track the state of this client
  private var _state : PigClientState = Connected

  // Private variables for the Pig level handlers
  private var _onPigClientMsgReceived : PigClientMessageReceivedHandler = null
  private var _onPigClientDisconnect : PigClientEndpointDisconnectHandler = null

  // Lock for the handlers
  private[this] val msglock = new Object()
  private[this] val dislock = new Object()

  // Go ahead and pre-populate the the client address
  private[this] val _clientAddress = s"${_channel.address}:${_channel.port}".replace("""/""", "")

  // Helper method for pretty printing of client endpoint address
  def getClientAddress = _clientAddress

  // The getters/setters for the handlers
  def onMessageReceived = msglock.synchronized { _onPigClientMsgReceived }
  def onClientDisconnect = dislock.synchronized { _onPigClientMsgReceived }

  def onMessageReceived_=(handler: PigClientMessageReceivedHandler) = msglock.synchronized { _onPigClientMsgReceived = handler}
  def onClientDisconnect_=(handler: PigClientEndpointDisconnectHandler) = dislock.synchronized { _onPigClientDisconnect = handler}

  // Allow consumers to see the current state
  def state = _state

  // Allow the consumer to send a message
  def sendMessage(msg: IpcMessage): Unit = { _channel sendMessage msg }

  // Private raise methods
  private def raiseOnMsgReceived(msg: IpcMessage) = msglock.synchronized { if (_onPigClientMsgReceived != null) _onPigClientMsgReceived(msg, this) }
  private def raiseOnClientDisconnect() = dislock.synchronized { if (_onPigClientDisconnect != null) _onPigClientDisconnect(this) }

  // Attach the handlers
  _channel.onEndpointDisconnected = onClientDisconnect
  _channel.onMessageReceived = onMessageReceivedHandler

  // Get or Set the WorkId
  def workId : UUID = _workId getOrElse new UUID(0, 0)
  def workId_=(id: UUID) = _workId = Some(id)

  // Handle closing the client
  def close() : Unit = {
    _state = Disconnected
    _channel close()
  }

  // Handle client timeout
  // Todo: Implement this method
  protected[PigSupport] def handleClientTimeout() : Unit = {
    ???
  }

  // The onMessageReceived handler
  private def onMessageReceivedHandler(msg: IpcMessage) : Unit = {
    msg match {
      case StubOnline(_) =>       _state = WorkRequested
      case PigResult(_) =>        _state = Finished
      case RemoteException(_) =>  _state = Error
      case _ => /* No-Op */
    }
    raiseOnMsgReceived(msg)
  }

  // The onClientDisconnect handler
  private def onClientDisconnect(c: IpcChannel) : Unit = {
    raiseOnClientDisconnect()
    _state = Disconnected
  }

}
