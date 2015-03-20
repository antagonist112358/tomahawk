package net.mentalarray.doozie.PigSupport


/**
 * Created by kdivincenzo on 2/19/15.
 */
protected[PigSupport] trait PigClientTracker { self: PigServer =>

  private final val clientTimeoutSeconds = 10
  private final val clientMonitorTime = 2000

  // The tracker timer
  private val _timerProc = Timer(clientMonitorTime, true) {

    // Copy local the clients list
    val clients = self.clientsList atomicFunc { _.map(x => x) }

    // Iterate through the clients
    for (c <- clients) {
      if (c hasExpired(clientTimeoutSeconds)) {
        // Log
        self.Log error s"Client ${c.getClientAddress} timed out."
        // Client has timed out
        c handleClientTimeout
      } else {
        // Send an "are you there" message
        c sendMessage CheckOnline()
      }
    }

  }

  /**
   * Stops the client tracker
   */
  def stopTracker(): Unit = {
    // Stop the timeout-proc
    _timerProc.stop()
  }

  /**
   * Starts the client tracker
   */
  def startTracker(): Unit = {
    // Start the timeout-proc
    _timerProc.start()
  }
  
}
