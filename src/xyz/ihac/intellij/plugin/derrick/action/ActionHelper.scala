package xyz.ihac.intellij.plugin.derrick.action

import xyz.ihac.intellij.plugin.derrick.logging.Logger

trait ActionHelper {
  type LogLevel = String
  val INFO_LEVEL: LogLevel = "info"
  val WARN_LEVEL: LogLevel = "warn"
  val ERROR_LEVEL: LogLevel = "error"

  type Status = Int
  val READY: Status = 0
  val RUNNING: Status = 1
  val FAILED: Status = 2
  val DONE: Status = 3
  val CANCELLED: Status = 4

  val action: String
  // BE CAREFUL that status is not thread-safe.
  private var status: Status = READY

  def start(): Unit = {
    status = RUNNING
    log(s"$action action start...", INFO_LEVEL)
  }
  def cancel(): Unit = {
    status = CANCELLED
    log(s"$action action cancelled.", INFO_LEVEL)
  }
  def done(): Unit = {
    status = DONE
    log(s"$action action done.", INFO_LEVEL)
  }
  def doneFrom(message: String): Unit = {
    log(message, INFO_LEVEL)
    done()
  }
  def fail(): Unit = {
    status = FAILED
    log(s"$action action failed.", INFO_LEVEL)
  }
  def failFrom(message: String): Unit = {
    log(message, ERROR_LEVEL)
    fail()
  }
  def isRunning: Boolean = status == RUNNING
  def log(message: String, level: LogLevel): Unit = level match {
    case ERROR_LEVEL => Logger.error(action, message)
    case WARN_LEVEL => Logger.warn(action, message)
    case INFO_LEVEL | _ => Logger.info(action, message)
  }
}
