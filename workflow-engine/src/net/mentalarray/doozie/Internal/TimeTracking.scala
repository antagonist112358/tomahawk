package net.mentalarray.doozie.Internal

import org.joda.time.{DateTime, Duration, Period}

/**
 * Created by kdivincenzo on 2/19/15.
 */

/**
 * Atomic tracking of chronological events.
 */
trait TimeTracking {

  // Initialize the DateTime signature to the "now" time
  /*** Note this variable can not be a value, despite what Idea might be saying ***/
  private var dtSig = DateTime.now

  // Atomic wrapper around the time signature
  private[this] val lock = new AnyRef

  /**
   * Causes this time tracked object to update its last touch time (signature time)
   */
  def update() : Unit = lock.synchronized { dtSig = DateTime.now }

  def getTimeSinceUpdate : Duration = {
    val diff = lock.synchronized { new Period(dtSig, DateTime.now) }
    diff.toStandardDuration
  }

  /**
   * Checks if this time tracked object has expired (passed its timeout)
   * @param seconds The timeout period
   * @return True if expired, false otherwise
   */
  def hasExpired(seconds: Int) : Boolean = if (getTimeSinceUpdate.toStandardSeconds.getSeconds > seconds) true else false

}
