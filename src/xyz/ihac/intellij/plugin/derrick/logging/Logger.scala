package xyz.ihac.intellij.plugin.derrick.logging

import com.intellij.icons.AllIcons
import com.intellij.notification._

object Logger {
  Notifications.Bus.register("Derrick", NotificationDisplayType.NONE)

  def info(action: String, message: String): Unit = {
    val notification = new Notification(
      "Derrick",
      AllIcons.General.Run,
      "Derrick",
      action,
      message,
      NotificationType.INFORMATION,
      NotificationListener.URL_OPENING_LISTENER
    )
    Notifications.Bus.notify(notification)
  }

  def warn(action: String, message: String): Unit = {
    val notification = new Notification(
      "Derrick",
      AllIcons.General.Run,
      "Derrick",
      action,
      message,
      NotificationType.WARNING,
      null
    )
    Notifications.Bus.notify(notification)
  }

  def error(action: String, message: String): Unit = {
    val notification = new Notification(
      "Derrick",
      AllIcons.General.Run,
      "Derrick",
      action,
      message,
      NotificationType.ERROR,
      null
    )
    Notifications.Bus.notify(notification)
  }
}
