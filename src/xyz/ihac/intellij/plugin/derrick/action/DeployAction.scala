package xyz.ihac.intellij.plugin.derrick.action

import java.io.File
import java.nio.file.{Path, Paths}

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindowManager
import io.kubernetes.client.apis.CoreV1Api
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}
import xyz.ihac.intellij.plugin.derrick.common.{Image, MyBoolean}
import xyz.ihac.intellij.plugin.derrick.core.Flow
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.util.Command


class DeployAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)

    Logger.info("Deploy", "<br>deploy action start...</br>")
    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
    val flow = new Flow("Deploy", option, projOption)

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
    val api = new CoreV1Api();

    flow.push2Remote(image, option)
    Logger.info("Deploy", "<br>deploy action done.</br>")
    // deploy2Cloud(config)
  }
}
