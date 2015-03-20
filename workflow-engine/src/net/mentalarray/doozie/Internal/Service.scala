package net.mentalarray.doozie.Internal

/**
 * Created by kdivincenzo on 2/18/15.
 */
trait Service extends Logging {

  /**
   * The service name for this service.
   * @return The name
   */
  def name : String

  /**
   * Starts this particular service.
   */
  def start() : Unit

  /**
   * Stops this particular service.
   */
  def stop() : Unit


}
