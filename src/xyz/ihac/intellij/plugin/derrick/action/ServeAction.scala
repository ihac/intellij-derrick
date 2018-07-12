package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.common.{Image, MyBoolean}
import xyz.ihac.intellij.plugin.derrick.docker.Docker
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.ExternalTask
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

class ServeAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    /**
      * Prepares for serve action.
      */
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)
    Logger.info("Serve", "serve action start...")

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

    /**
      * Popups a dialog to generate configurations for this action.
      * No need to use UITask since it's in Event Dispatch Thread.
      */
    val configDialog = new DerrickConfigForm(project, "Serve")
    configDialog.show()
    // return if the dialog does not exist with OK.
    if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE) {
      Logger.info("Serve", "serve action cancelled.")
      return
    }
    val isRebuild = configDialog.getIsRebuild
    val imageName = configDialog.getImageName

    /**
      * [External API] Rebuilds image if needed.
      */
    val imageRebuildFuture =
      if (isRebuild) {
        new ExternalTask(() => {
          try {
            Logger.info("Serve", "start to build image <%s>".format(imageName))
            val imageId = new Docker(option.getDockerExecPath)
              .buildImage(projOption.getWorkDir, imageName, true)
            Logger.info("Serve", "succeed in building image <%s>:<%s>".format(imageName, imageId))
            Some(imageId)
          } catch {
            case e: Exception => {
              Logger.error("Serve", "error in building image from Dockerfile: %s".format(e.getMessage))
              Logger.info("Serve", "serve action failed.")
              None
            }
          }
        }).run
      }
      else null

    /**
      * [External API] Starts container from specified image.
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
        Logger.info("Serve", "start to run container from image <%s>".format(imageName))
        val containerId = new Docker(option.getDockerExecPath)
          .runContainer(imageName)
        Logger.info("Serve", "succeed in running container <%s>".format(containerId))
        Logger.info("Serve", "serve action done.")
        Some(containerId)
      } catch {
        case e: Exception => {
          Logger.error("Serve", "error in running container from image <%s>: %s".format(imageName, e.getMessage))
          Logger.info("Serve", "serve action failed.")
          None
        }
      }
    }).run
  }
}
