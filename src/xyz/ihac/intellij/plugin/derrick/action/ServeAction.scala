package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}
import xyz.ihac.intellij.plugin.derrick.common.{Image, MyBoolean}
import xyz.ihac.intellij.plugin.derrick.core.Flow
import xyz.ihac.intellij.plugin.derrick.logging.Logger

class ServeAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)

    Logger.info("Serve", "<html><strong>serve action start...</strong></html>")
    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
    val flow = new Flow("Serve", option, projOption)
    // configuration
    val config = flow.initConfig(project);
    if (config == null) {
      Logger.info("Serve", "<html><strong>serve action canceled.strong></html>")
      return
    }

    val image =
      if (config("isRebuild").asInstanceOf[MyBoolean].toBoolean) {
        val spec = flow.generateSpecs(project, config)
        Logger.info("Serve", "\t<html>start to rebuild image...</html>")
        val img = flow.buildImage(spec, config)
        Logger.info("Serve", "\t<html>build image done.</html>")
        img
      }
      else {
        println(config("imageId"))
        val Array(name, tag) = config("imageId").toString.split(":")
        new Image(name, tag, "")
      }

    // run container
    val messages = flow.runContainer(image, config) match {
      case null => "Fail to start container from image <%s>".format(config("imageId"))
      case app => "Start container %s from image <%s> successfully".format(app.id, config("imageId"))
    }
    Logger.info("Serve", "\t%s".format(messages))
//    Messages.showMessageDialog(project, messages, "Result", Messages.getInformationIcon)
    Logger.info("Serve", "<b>serve action done.</b>")
  }
}
