package net.mentalarray.doozie.Internal.RPC

import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.UUID

import org.apache.commons.lang.SerializationUtils

import scala.util.{Failure, Success, Try}

/**
 * Created by kdivincenzo on 2/4/15.
 */
protected object Serializer {

  import java.nio.ByteBuffer

  def getBytes(i: Int) : Array[Byte] = {
    ByteBuffer.allocate(4).putInt(i).array
  }

  def getBytes(l: Long) : Array[Byte] = {
    ByteBuffer.allocate(8).putLong(l).array
  }

  def getBytes(id : UUID) : Array[Byte] = {
    //Seq(getBytes(id.getMostSignificantBits), getBytes(id.getMostSignificantBits)).flatten.toArray
    ByteBuffer.allocate(16)
    .putLong(id.getMostSignificantBits)
    .putLong(id.getLeastSignificantBits)
    .array
  }

  def getBytes(s: String) : Array[Byte] = s.getBytes(StandardCharsets.UTF_8)

  def readInt(stream: Array[Byte], pos: Int) = {
    ByteBuffer.wrap(stream).getInt(pos)
  }

  def stringFromBytes(b : Array[Byte]) : String = new String(b, StandardCharsets.UTF_8)

  def serialize[B <: Serializable](target: B) : Try[Array[Byte]] = {
    try {
      val bytes = SerializationUtils.serialize(target)
      Success(bytes)
    } catch {
      case t: Throwable => Failure(t)
    }
  }

  def deserialize[B <: Serializable](source: Array[Byte]) : Try[B] = {
    try {
      val exObj = SerializationUtils.deserialize(source)
      val res = exObj.asInstanceOf[B]
      Success(res)
    } catch {
      case t: Throwable => Failure(t)
    }
  }

}
