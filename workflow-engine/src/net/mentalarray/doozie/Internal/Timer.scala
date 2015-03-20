/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mentalarray.doozie.Internal

import java.util.concurrent._

/**
 * A Timer allows for a method to be executed on a periodic basis.
 * @param interval The number of milliseconds between each execution of the 'timerFn'.
 * @param repeats Indicates whether the timer will continue to invoke the 'timerFn' after the first initial invocation.
 *                Note: The first initial invocation is implicit.
 * @param timerFn The function which will be executed when the timer's interval period expires.
 */
protected[workflow] class Timer(interval: Int, repeats: Boolean = true, timerFn: => Unit) {

  /**
   * Used to create a new Runnable which will be repeatedly executed.
   */
  private lazy val timerCommand = new Runnable {
    override def run(): Unit = timerFn
  }

  /**
   * Used to create a new Runnable which will be executed only once. Needs the extra effect of shutting down
   * the ScheduleExecutorService which was tasked with running the timer.
   * @param svc The ScheduleExecutorService which was tasked with running the timer.
   * @return The Runnable implementation which encapsulates both functions (stopping the service and executing
   *         the timer function.
   */
  private def oneShotCommand(svc: ScheduledExecutorService) = new Runnable {
    override def run(): Unit = {
      // Dispose / stop the executor
      svc.shutdown
      // Execute the timer function
      timerFn
    }
  }

  /**
   * Lazily initialize the ScheduleExecutorService, depending on the 'repeats' flag, to the
   * proper type of scheduled future.
   */
  private lazy val _timer = {
    // Creates a new ScheduleExecutorService for running the timer
    val t = Executors.newScheduledThreadPool(4)
    // Create either a recurring timer or a one-shot scheduled execution
    if (repeats)
      t.scheduleAtFixedRate(timerCommand, interval, interval, TimeUnit.MILLISECONDS)
    else
      t.schedule(oneShotCommand(t), interval, TimeUnit.MILLISECONDS)
    // Return the ScheduleExecutorService
    t
  }

  /**
   * Starts either the one-shot or recurrent invocation of the timerFunction.
   */
  def start() : Unit = _timer

  /**
   * Either cancels a one-shot scheduled invocation (if it has not already been invoked) or
   * stops the recurrent invocation of the timerFunction.
   */
  def stop() : Unit = _timer.shutdown

  /**
   * Specifes that the timer is executed more than once, on a periodic schedule (repeating)
   * @return true if the timer repeats, false otherwise
   */
  def doesRepeat = repeats
}

/**
 * Companion object for Timer class.
 */
object Timer {

  /**
   * Creates a new instance of the Timer class.
   * @param interval The initial delay and recurrent interval of invocation (in milliseconds).
   * @param repeats A flag which determines if the timerFunction will be invoked once or will continue being
   *                invoked every 'interval' milliseconds.
   * @param op An expression which evaluates to Unit - This is the timerFunction which will be invoked.
   * @return An instance of the Timer class.
   */
  def apply(interval: Int, repeats: Boolean = true)(op: => Unit) = new Timer(interval, repeats, op)

  /**
   * Creates a new instance of the Timer class and starts it.
   * @param interval The initial delay and recurrent interval of invocation (in milliseconds).
   * @param repeats A flag which determines if the timerFunction will be invoked once or will continue being
   *                invoked every 'interval' milliseconds.
   * @param op An expression which evaluates to Unit - This is the timerFunction which will be invoked.
   * @return An instance of the Timer class, which has already been started.
   */
  def startNew(interval: Int, repeats: Boolean = true)(op: => Unit) : Timer = {
    val timer = new Timer(interval, repeats, op)
    timer.start; timer
  }
}