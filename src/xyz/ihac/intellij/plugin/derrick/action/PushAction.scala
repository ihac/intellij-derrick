package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}
import xyz.ihac.intellij.plugin.derrick.common.{Image, MyBoolean}
import xyz.ihac.intellij.plugin.derrick.core.Flow
import xyz.ihac.intellij.plugin.derrick.logging.Logger


class PushAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)

    Logger.info("Push", "<b>push action start...</b>")
    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
    val flow = new Flow("Push", option, projOption)

    // configure
    val config = flow.initConfig(project);
    if (config == null) return

    // build image
    val image =
      if (config("isRebuild").asInstanceOf[MyBoolean].toBoolean) {
        val spec = flow.generateSpecs(project, config)
        flow.buildImage(spec, config)
      }
      else {
        val Array(name, tag) = config("imageId").toString.split(":")
        new Image(name, tag, "")
        // ImageLoader.load(config)
      }

    // deploy to cloud
    flow.push2Remote(image, option)
//    Logger.info("Push", "<b>push action done.</b>")
  }
}

