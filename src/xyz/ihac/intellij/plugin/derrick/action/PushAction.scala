package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.common.{Image, MyBoolean}
import xyz.ihac.intellij.plugin.derrick.docker.Docker
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.{ExternalTask, ImageNameFormat}
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

class PushAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    /**
      * Prepares for push action.
      */
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)
    Logger.info("Push", "push action start...")

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

    /**
      * Popups a dialog to generate configurations for this action.
      * No need to use UITask since it's in Event Dispatch Thread.
      */
    val configDialog = new DerrickConfigForm(project, "Push")
    configDialog.show()
    // return if the dialog does not exist with OK.
    if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE) {
      Logger.info("Push", "push action cancelled.")
      return
    }
    val isRebuild = configDialog.getIsRebuild
    val imageName = configDialog.getImageName

    import Image._
    val registry = option.getRegistryByUrlAndUsername(imageName.url, imageName.username)
    if (registry == null) {
      Logger.error("Push", "no suitable registry configuration is found for pushing image <%s>".format(imageName))
      Logger.info("Push", "push action failed.")
      return
    }

    /**
      * [External API] Rebuilds image if needed.
      */
    val imageRebuildFuture =
      if (isRebuild) {
        new ExternalTask(() => {
          try {
            Logger.info("Push", "start to build image <%s>".format(imageName))
            val imageId = new Docker(option.getDockerExecPath)
              .buildImage(projOption.getWorkDir, imageName, true)
            Logger.info("Push", "succeed in building image <%s>:<%s>".format(imageName, imageId))
            Some(imageId)
          } catch {
            case e: Exception => {
              Logger.error("Push", "error in building image from Dockerfile: %s".format(e.getMessage))
              Logger.info("Push", "push action failed.")
              None
            }
          }
        }).run
      }
      else null

    /**
      * [External API] Pushes image to remote registry.
      */
    new ExternalTask(() => {
      // wait for rebuilding image finished.
      if (imageRebuildFuture != null) {
        imageRebuildFuture.get() match {
          case Some(_) =>
          case None => return
        }
      }
      try {
        Logger.info("Push", "start to push image <%s> to remote registry".format(imageName))
        new Docker(option.getDockerExecPath, registry)
          .pushImage(imageName, true)
        Logger.info("Push", "succeed in pushing image <%s>".format(imageName))
        Logger.info("Push", "push action done.")
      } catch {
        case e: Exception => {
          Logger.error("Push", "error in pushing image to registry <%s>: %s".format(registry.getUrl, e.getMessage))
          Logger.info("Push", "push action failed.")
        }
      }
    }).run
  }
}

