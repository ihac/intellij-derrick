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
import xyz.ihac.intellij.plugin.derrick.core.{CloudDeployer, Flow}
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.util.Command


class DeployAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    try {
      /*
       * preparation.
       */
      val project = e.getProject
      val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
      if (!eventLog.isVisible)
        eventLog.show(null)
      Logger.info("Deploy", "deploy action start...")

      val option = ServiceManager.getService(classOf[DerrickOptionProvider])
      val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
      val flow = new Flow("Deploy", option, projOption)

      /*
       * generate configuration for deploy action.
       */
      val config = flow.initConfig(project);
      if (config == null) {
        Logger.info("Deploy", "deploy action cancelled.")
        return
      }

      /*
       * deploy app to cloud.
       */
      flow.deploy2Cloud(config)
      Logger.info("Deploy", "deploy action done.")
    } catch {
      case e: Exception => Logger.error("Deploy", "deploy action failed: %s".format(e.getMessage))
      case _ => Logger.error("Deploy", "deploy action failed due to unknown error(s). Contact author for support.")
    }
  }
}
