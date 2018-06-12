package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}
import xyz.ihac.intellij.plugin.derrick.common.{Image, MyBoolean}
import xyz.ihac.intellij.plugin.derrick.core.Flow
import xyz.ihac.intellij.plugin.derrick.logging.Logger

class ServeAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    try {
      /*
       * preparation.
       */
      val project = e.getProject
      val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
      if (!eventLog.isVisible)
        eventLog.show(null)
      Logger.info("Serve", "serve action start...")

      val option = ServiceManager.getService(classOf[DerrickOptionProvider])
      val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
      val flow = new Flow("Serve", option, projOption)

      /*
       * generate configuration for serve action.
       */
      val config = flow.initConfig(project);
      if (config == null) {
        Logger.info("Serve", "serve action cancelled.")
        return
      }

      /*
       * get image info by either rebuilding a new image or reusing the existing image.
       */
      val image =
        if (config("isRebuild").asInstanceOf[MyBoolean].toBoolean) {
          val spec = flow.generateSpecs(project, config)
          val img = flow.buildImage(spec, config)
          img
        }
        else {
          val Array(name, tag) = config("imageId").toString.split(":")
          new Image(name, tag, "")
        }

      /*
       * run container from the image.
       */
      val messages = flow.runContainer(image, config) match {
        case app => "Start container %s from image <%s> successfully".format(app.id, config("imageId"))
      }
      Logger.info("Serve", "\t%s".format(messages))
      Logger.info("Serve", "serve action done.")
    } catch {
      case e: Exception => Logger.error("Serve", "serve action failed: %s".format(e.getMessage))
      case _ => Logger.error("Serve", "serve action failed due to unknown error(s). Contact author for support.")
    }
  }
}
