package xyz.ihac.intellij.plugin.derrick.logging

import com.intellij.notification._

/** Enables plugin to log in the EventLog window of IDE.
  *
  */
object Logger {
  val logger = new NotificationGroup("Derrick", NotificationDisplayType.NONE, true)

  /** Logs as an information.
    *
    * @param action current action which this log belongs to.
    * @param message detailed message of this log.
    */
  def info(action: String, message: String): Unit = {
    val notification = logger.createNotification(
      "Derrick",
      action,
      message,
      NotificationType.INFORMATION
    )
    Notifications.Bus.notify(notification)
  }

  /** Logs as a warning.
    *
    * @param action current action which this log belongs to.
    * @param message detailed message of this log.
    */
  def warn(action: String, message: String): Unit = {
    val notification = logger.createNotification(
      "Derrick",
      action,
      message,
      NotificationType.WARNING
    )
    Notifications.Bus.notify(notification)
  }

  /** Logs as an error.
    *
    * @param action current action which this log belongs to.
    * @param message detailed message of this log.
    */
  def error(action: String, message: String): Unit = {
    val notification = logger.createNotification(
      "Derrick",
      action,
      message,
      NotificationType.ERROR
    )
    Notifications.Bus.notify(notification)
  }
}
