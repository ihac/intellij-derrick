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
    try {
      /*
       * preparation.
       */
      val project = e.getProject
      val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
      if (!eventLog.isVisible)
        eventLog.show(null)
      Logger.info("Push", "push action start...")

      val option = ServiceManager.getService(classOf[DerrickOptionProvider])
      val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
      val flow = new Flow("Push", option, projOption)

      /*
       * generate configuration for push action.
       */
      val config = flow.initConfig(project);
      if (config == null) {
        Logger.info("Push", "push action cancelled.")
        return
      }

      /*
       * get image info by either rebuilding a new image or reusing the existing image.
       */
      val image =
        if (config("isRebuild").asInstanceOf[MyBoolean].toBoolean) {
          val spec = flow.generateSpecs(project, config)
          flow.buildImage(spec, config)
        }
        else {
          val Array(name, tag) = config("imageId").toString.split(":")
          new Image(name, tag, "")
        }

      /*
       * push image to remote registry.
       */
      flow.push2Remote(config, image, option)
      Logger.info("Push", "push action done.")
    } catch {
      case e: Exception => Logger.error("Push", "push action failed: %s".format(e.getMessage))
      case _ => Logger.error("Push", "push action failed due to unknown error(s). Contact author for support.")
    }
  }
}

