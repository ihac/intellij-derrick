package xyz.ihac.intellij.plugin.derrick.logging

import com.intellij.icons.AllIcons
import com.intellij.notification._

object Logger {
  val logger = new NotificationGroup("Derrick", NotificationDisplayType.NONE, true)

  def info(action: String, message: String): Unit = {
    val notification = logger.createNotification(
      "Derrick",
      action,
      message,
      NotificationType.INFORMATION
    )
    Notifications.Bus.notify(notification)
  }

  def warn(action: String, message: String): Unit = {
    val notification = logger.createNotification(
      "Derrick",
      action,
      message,
      NotificationType.WARNING
    )
    Notifications.Bus.notify(notification)
  }

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
