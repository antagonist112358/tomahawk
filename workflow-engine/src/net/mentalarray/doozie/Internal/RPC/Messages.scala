package net.mentalarray.doozie.Internal.RPC

import java.util.UUID

import scala.util.{Failure, Success}

/**
 * Created by kdivincenzo on 2/4/15.
 */
sealed trait IpcMessage {

  /**
   * Get the byte array representation of the message
   * @return A Byte array containing the bytes necessary to rebuild the message across IPC boundaries
   */
  def toBytes : Array[Byte]

}

/**
 * All the messages that the stub and the parent process can exchange
 */
object Messages extends Logging
{

  private final val TrueByte  = 1 : Byte
  private final val FalseByte = 0 : Byte

  abstract class MessageBase protected() extends IpcMessage {

    val messageCode : Byte

    def toBytes : Array[Byte] = {
      val internalBytes = serialize()
      Seq(Array(messageCode), internalBytes).flatten.toArray
    }

    protected def serialize() : Array[Byte]

  }

  def deserialize(bytes: Array[Byte]) : IpcMessage = {

    // Setup the ByteStream
    val stream = new ByteStream(bytes)

    // Get the header byte and the length
    val hByte = stream.readByte

    // Depending on the message type
    hByte match {
      // Is Online
      case 0 => IsOnline()
      // CheckOnline
      case 1 => CheckOnline()
      // PigTaskData
      case 2 => {
        // Read the contents
        val payloadBytes = stream.readToEnd
        // Convert to a string
        val payload = Serializer.stringFromBytes(payloadBytes)
        // Debug
        Log debug s"PigTask XML: $payload"
        // Convert to XML
        val pigXml = scala.xml.XML.loadString(payload)
        // Produce -> PigTask
        PigTaskData(PigTask.fromXml(pigXml))
      }
      // PigResult
      case 3 => {
        // Get the next (final) byte
        val bloatedBool = stream.readByte
        // Check
        if (bloatedBool == TrueByte) PigResult(true)
        else if (bloatedBool == FalseByte) PigResult(false)
        else throw new Exception(s"Unknown PigResult: $bloatedBool")
      }
      // RemoteException
      case 4 => {
        // Read the exception data
        val exData = stream.readToEnd
        // Deserialize into an exception
        val ex = Serializer.deserialize[Exception](exData)
        // Produce -> StubException or error
        ex match {
          case Success(e) => RemoteException(e)
          case Failure(t) => throw t
        }
      }
       // StubOnline
      case 5 => {
        // Read the UUID
        val uuid = stream.readUUID
        // Produce -> StubOnline
        StubOnline(uuid)
      }
      // All others
      case _ => throw new Exception(s"Unknown Message Code: $hByte")
    }

  }

  // Message code is one byte, this one will be (Byte)0
  case class IsOnline() extends MessageBase {
    override val messageCode: Byte = 0
    override def serialize(): Array[Byte] = Array()
  }

  case class CheckOnline() extends MessageBase {
    override val messageCode: Byte = 1
    override def serialize(): Array[Byte] = Array()
  }
  
  case class PigTaskData(pigTask: PigTask) extends MessageBase {
    private lazy val bytes = {
      val taskXml = pigTask.toXml.toString
      taskXml.getBytes
    }
    override val messageCode: Byte = 2
    override def serialize() : Array[Byte] = bytes
  }

  case class PigResult(didPass: Boolean) extends MessageBase {
    override val messageCode: Byte = 3
    override def serialize(): Array[Byte] = {
      val bloatedBool: Byte = if (didPass == true) TrueByte else FalseByte
      Array(bloatedBool)
    }
  }

  case class RemoteException(e: Exception) extends MessageBase {
    private lazy val maybeBytes = Serializer.serialize(e)
    override val messageCode: Byte = 4
    override def serialize() : Array[Byte] = maybeBytes match {
      case Success(arr) => arr
      case Failure(e) => throw e
    }
  }

  case class StubOnline(id: UUID) extends MessageBase {
    private lazy val bytes = Serializer.getBytes(id)
    override val messageCode: Byte = 5
    override def serialize() : Array[Byte] = bytes
  }
}

