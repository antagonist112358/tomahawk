package net.mentalarray.doozie

import scala.collection.mutable

/**
 * Created by kdivincenzo on 9/7/14.
 */

class VariablesMap() {

  val internalList = mutable.Map.empty[String,Any]

  def store[T <: AnyVal](key: String, value: T) = {
    internalList += key.toLowerCase -> value
  }

  def store(key: String, value: String) = {
    internalList += key.toLowerCase -> value
  }

  def retrieve[T <: AnyVal](key: String) : T = {
    internalList(key.toLowerCase).asInstanceOf[T]
  }

  def variablesCount : Int = {
    internalList.size
  }
}
