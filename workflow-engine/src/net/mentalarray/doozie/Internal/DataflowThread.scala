package net.mentalarray.doozie.Internal

/**
 * Created by kdivincenzo on 2/4/15.
 */
sealed class DataflowThread private (proc: => Unit, exHandler: Exception => Unit = null) {

  private class RunnableThread(action: Unit => Unit) extends Runnable {
    override def run: Unit = {
      try {
        action()
      } catch {
        case interrupt : InterruptedException => { /* Thread interruption is legal and not an error condition */ }
        case e: Exception => if (exHandler != null) exHandler(e)
      }
    }
  }

  private val actualThread = new java.lang.Thread(new RunnableThread(_ => proc))

  // Start on ctor
  actualThread.start()

  // Allow user to stop the thread (using thread interrupt)
  def stop() = {
    actualThread.interrupt
    actualThread.join
  }
}

private[workflow] object DataflowThread {

  def apply(threadProc: => Unit) = new DataflowThread(threadProc)

}
