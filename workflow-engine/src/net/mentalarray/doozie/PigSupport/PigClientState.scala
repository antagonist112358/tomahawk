package net.mentalarray.doozie.PigSupport

/**
 * Created by kdivincenzo on 2/18/15.
 */

sealed trait PigClientState { }
object PigClientState  {
  case object Connected extends PigClientState
  case object WorkRequested extends PigClientState
  case object Finished extends PigClientState
  case object Disconnected extends PigClientState
  case object Error extends PigClientState
}
