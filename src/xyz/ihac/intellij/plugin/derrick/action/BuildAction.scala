package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.icons.AllIcons
import com.intellij.notification._
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.{ToolWindowAnchor, ToolWindowManager}
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}
import xyz.ihac.intellij.plugin.derrick.core.Flow
import xyz.ihac.intellij.plugin.derrick.util.Command


class BuildAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getProject
    val presentation = e.getPresentation
    val toolWindowManager = ToolWindowManager.getInstance(project)

    Notifications.Bus.register("Derrick", NotificationDisplayType.NONE)
    val notification = new Notification("Derrick",
      AllIcons.General.Run,
      "Title",
      "SubTitle",
      "Content",
      NotificationType.INFORMATION,
      null)
    Notifications.Bus.notify(notification)
    EventLog.toggleLog(project, notification)

    val notification2 = new Notification("Derrick",
      AllIcons.General.Run,
      "Title",
      "SubTitle",
      "Error Content",
      NotificationType.ERROR,
      null)
    Notifications.Bus.notify(notification2)

    var toolWindow = toolWindowManager.getToolWindow("DerrickPlugin")
    if (toolWindow == null)

    presentation.setEnabledAndVisible(toolWindow.isActive)

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
    val flow = new Flow("Build", option, projOption)

    // configuration
    val config = flow.initConfig(project);
    if (config == null) return
    // generate specs (Dockerfile, Kubernetes yaml).
    val spec = flow.generateSpecs(project, config);

    // build image (Docker build).
    val messages = flow.buildImage(spec, config) match {
      case null => "Fail to build image %s".format(config("imageId"))
      case img => "Build image %s(%s) successfully".format(config("imageId"), img.getId)
    }
    Messages.showMessageDialog(project, messages, "Result", Messages.getInformationIcon)
    EventLog.toggleLog(project, notification)

  }
}
