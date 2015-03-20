package net.mentalarray.doozie.Internal.RPC

import java.nio.ByteBuffer
import java.util.UUID

import scala.reflect.ClassTag

/**
 * Created by kdivincenzo on 2/5/15.
 */
protected class ByteStream(bytes: Array[Byte]) {

  // Wrap the byte array
  private val byteBuffer = ByteBuffer.wrap(bytes)
  private val length = bytes.size

  // Keep track of the current index
  private var index = 0

  // Read Methods
  def readByte = {
    val b = byteBuffer.get(index)
    index +=1
    b
  }

  def readInt = {
    val i = byteBuffer.getInt(index)
    // increment
    index += 4
    // Return
    i
  }

  def readUUID = {
    // Read high bits
    val highBits = byteBuffer.getLong(index)
    // Increment
    index += 8
    // Read low bits
    val lowBits = byteBuffer.getLong(index)
    // Increment
    index += 8
    // Return
    new UUID(highBits, lowBits)
  }

  def readToEnd = {
    byteBuffer.array.range(index, length - index)
  }


  // Array helper methods
  implicit class ArrayExtensions[B](arr: Array[B])(implicit tag : ClassTag[B]) {

    def range(startIndex: Int, count: Int) = {
      val output = new Array[B](count)
      for(i <- startIndex to count) {
        output(i - startIndex) = arr(i)
      }
      output
    }

  }

}


