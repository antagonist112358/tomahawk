package net.mentalarray.doozie.Internal

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

/**
 * Created by kdivincenzo on 1/6/2015.
 */

private final object ThreadPool {
  /**
   * Represents a wrapped action which will be executed on the ThreadPool
   * @param action An expression which produces Unit when invoked.
   */
  private class ThreadPoolAction(action: => Unit) extends Runnable with Logging {
    /**
     * Run is the method which will be invoked by the thread pool.
     * All this method does is call the wrapped delegate in a try/catch pattern.
     */
    override def run : Unit = try { action } catch { case e: Exception => Log.error("Unhandled ThreadPool Exception: ", e) }
  }

  // The actual, honest-to-god thread-pool
  private lazy val pool = Executors.newFixedThreadPool(16)
  // The execution context for futures and shit
  protected[workflow] lazy val context = ExecutionContext.fromExecutor(pool)

  /**
   * Executes a runnable on the thread-pool
   * @param runForest The runnable to execute
   */
  private def execute(runForest : Runnable) = pool.execute(runForest)

  /**
   * Runs a delegate expression on the thread-pool.
   * @param action An expression containing the code to be executed by the thread-pool
   */
  def fireAndForget(action: => Unit) : Unit = execute ( new ThreadPoolAction(action) )
}
