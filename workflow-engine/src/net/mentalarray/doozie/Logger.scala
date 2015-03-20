package net.mentalarray.doozie

import org.apache.log4j.{Level, Logger => Log4JLoggerImpl}

/**
 * Created by kdivincenzo on 9/12/14.
 */
abstract class Logger {

  def debug(message: => String)
  def debug(message: => String, ex:Throwable)
  def debugValue[T](valueName: String, value: => T): T

  def info(message: => String)
  def info(message: => String, ex:Throwable)

  def warn(message: => String)
  def warn(message: => String, ex:Throwable)

  def error(ex:Throwable)
  def error(message: => String)
  def error(message: => String, ex:Throwable)

  def fatal(ex:Throwable)
  def fatal(message: => String)
  def fatal(message: => String, ex:Throwable)

}

sealed class Log4JLogger(loggerName: String) extends Logger {

  import org.apache.log4j.Level._

  private[this] val logger = Log4JLoggerImpl.getLogger(loggerName);

  override def debug(message: => String, ex:Throwable) = if (logger.isEnabledFor(DEBUG)) logger.debug(message,ex)

  override def debugValue[T](valueName: String, value: => T):T = {
    val result:T = value
    debug("Value Name: " + valueName + " -> " + result.toString)
    result
  }

  override def debug(message: => String) = if (logger.isEnabledFor(DEBUG)) logger.debug(message)

  override def info(message: => String) = if (logger.isEnabledFor(INFO)) logger.info(message)
  override def info(message: => String, ex:Throwable) = if (logger.isEnabledFor(INFO)) logger.info(message,ex)

  override def warn(message: => String) = if (logger.isEnabledFor(WARN)) logger.warn(message)
  override def warn(message: => String, ex:Throwable) = if (logger.isEnabledFor(WARN)) logger.warn(message,ex)

  override def error(ex:Throwable) = if (logger.isEnabledFor(ERROR)) logger.error(ex.toString,ex)
  override def error(message: => String) = if (logger.isEnabledFor(ERROR)) logger.error(message)
  override def error(message: => String, ex:Throwable) = if (logger.isEnabledFor(ERROR)) logger.error(message,ex)

  override def fatal(ex:Throwable) = if (logger.isEnabledFor(FATAL)) logger.fatal(ex.toString,ex)
  override def fatal(message: => String) = if (logger.isEnabledFor(FATAL)) logger.fatal(message)
  override def fatal(message: => String, ex:Throwable) = if (logger.isEnabledFor(FATAL)) logger.fatal(message,ex)
}

object Log4JLogger {

  def disableConsoleOutput : Unit = {
    Log4JLoggerImpl.getLogger("net.mentalarray.doozie").removeAppender("console")
  }

  def turnOffDefaultLogging() {
    if (Log4JLoggerImpl.getRootLogger.getLevel != Level.OFF) {
      Log4JLoggerImpl.getRootLogger.setLevel(Level.OFF)
    }
  }
}

trait Logging { self: AnyRef =>

  private val logImpl: Logger = new Log4JLogger(this.getClass.getName)

  def Log: Logger = logImpl

}