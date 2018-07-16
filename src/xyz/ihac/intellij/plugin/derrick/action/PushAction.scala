package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.common.Image
import xyz.ihac.intellij.plugin.derrick.docker.Docker
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.{BackgroundTask, WaitableUITask}
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

import scala.util.{Failure, Success, Try}

class PushAction extends AnAction with ActionHelper {
  val action = "Push"

  override def actionPerformed(e: AnActionEvent): Unit = {
    /**
      * Prepares for push action.
      */
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

    /**
      * [Flow] Procedure of Push action.
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
        * Checks whether there is a pre-defined registry configuration for the imageName.
        */
      assert(imageName != null)
      import Image._
      val registry = option.getRegistryByUrlAndUsername(imageName.url, imageName.username)
      if (registry == null)
        failFrom(s"no suitable registry configuration is found for pushing image <${imageName}>")
      if (!isRunning) return

      /**
        * [External API] Rebuilds image if needed.
        */
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
        * [External API] Pushes image to remote registry.
        */
      log(s"start to push image <${imageName}> to remote registry", INFO_LEVEL)
      val pushTry = Try {
        new Docker(option.getDockerExecPath, registry)
          .pushImage(imageName, true)
      }
      pushTry match {
        case Success(_) =>
          doneFrom(s"succeed in pushing image <${imageName}>")
        case Failure(e) =>
          failFrom(s"unexpected error in pushing image <${imageName}> to registry <${registry.getUrl}>: ${e.getMessage}")
      }
    }).run
  }
}

