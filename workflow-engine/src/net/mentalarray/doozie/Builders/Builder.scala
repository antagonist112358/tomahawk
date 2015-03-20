package net.mentalarray.doozie.Builders

/**
 * Created by kdivincenzo on 9/10/14.
 */

/** *
  * Common trait for all builders
  */
trait Builder[+A <: AnyRef] {

  // Must be implemented
  def build() : A

}
