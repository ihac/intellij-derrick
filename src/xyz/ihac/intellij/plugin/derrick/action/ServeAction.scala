package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.docker.Docker
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.{BackgroundTask, WaitableUITask}
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

import scala.util.{Failure, Success, Try}

class ServeAction extends AnAction with ActionHelper {
  val action = "Serve"

  override def actionPerformed(e: AnActionEvent): Unit = {
    /**
      * Prepares for serve action.
      */
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

    /**
      * [Flow] Procedure of Serve action.
      */
    new BackgroundTask(() => {
      start()

      /**
        * [UI Task] Popups a dialog to generate configurations for this action.
        */
      var isRebuild: Boolean = false
      var imageName: String = null
      new WaitableUITask(() => {
        val configDialog = new DerrickConfigForm(project, action)
        configDialog.show()
        // return if the dialog does not exist with OK.
        if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE)
          cancel()
        isRebuild = configDialog.getIsRebuild
        imageName = configDialog.getImageName
      }).run
      if (!isRunning) return

      /**
        * [External API] Rebuilds image if needed.
        */
      assert(imageName != null)
      if (isRebuild) {
        log(s"start to build image <${imageName}>", INFO_LEVEL)
        val rebuildTry = Try {
          new Docker(option.getDockerExecPath)
            .buildImage(projOption.getWorkDir, imageName, true)
        }
        rebuildTry match {
          case Success(imageId) =>
            log(s"succeed in building image <${imageName}> - <${imageId}>", INFO_LEVEL)
          case Failure(e) =>
            failFrom(s"unexpected error in building image from Dockerfile: ${e.getMessage}")
        }
      }
      if (!isRunning) return

      /**
        * [External API] Starts container from specified image.
        */
      log(s"start to run container from image <${imageName}>", INFO_LEVEL)
      val runContainerTry = Try {
        new Docker(option.getDockerExecPath)
          .runContainer(imageName)
      }
      runContainerTry match {
        case Success(containerId) =>
          doneFrom(s"succeed in running container <${containerId}> from image <${imageName}>")
        case Failure(e) =>
          failFrom(s"unexpected error in running container from image <${imageName}>: ${e.getMessage}")
      }
    }).run
  }
}
