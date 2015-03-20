package net.mentalarray.doozie

import org.joda.time.format.DateTimeFormat

import scala.reflect.ClassTag

/**
 * Created by bgilcrease on 9/10/14.
 * aka stolen from the internet
 */

abstract class StringConverter[T] {
  def convert(s: String): T
}

object StringConverter {

  def toConverter[T](converter: String => T): StringConverter[T] = new StringConverter[T] {
    override def convert(s: String): T = converter(s)
  }
  val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")

  implicit val intConverter = toConverter(_.toInt)
  implicit val arrayConverter = toConverter(_.toArray)
  implicit val booleanConverter = toConverter(_.toBoolean)
  implicit val listConverter = toConverter(_.toList)
  implicit val stringConverter = toConverter(_.toString)
  implicit val dateTimeConverter = toConverter(fmt.parseDateTime(_))

  def getStringAs[T : StringConverter : ClassTag](value: String): T = {
    val converter = implicitly[StringConverter[T]]
    val runtimeClass = implicitly[ClassTag[T]].runtimeClass

    try {
      converter convert value
    } catch {
      case _ : Throwable => throw new ClassCastException(s"Could not cast string '$value' to type: %s" format runtimeClass.getName)
    }
  }
}


