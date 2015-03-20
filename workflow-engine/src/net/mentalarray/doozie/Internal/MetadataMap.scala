package net.mentalarray.doozie.Internal

import scala.collection.mutable

/**
 * Created by kdivincenzo on 2/18/15.
 */
class MetadataMap {

  private val _storage = mutable.Map.empty[String, Any]
  private[this] val lock = new AnyRef

  def apply(key: String) : Any = lock.synchronized{ _storage(key) }

  def getAs[B](key: String) : B = lock.synchronized{ _storage(key).asInstanceOf[B] }

  def set(key: String, value: Any) = lock.synchronized{ _storage += key -> value }

}
