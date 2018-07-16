package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.derrick.Derrick
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.{BackgroundTask, WaitableUITask}
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class InitAction extends AnAction with ActionHelper {
  val action = "Init"

  override def actionPerformed(e: AnActionEvent): Unit = {
    /**
      * Prepares for init action.
      */
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

    /**
      * [Flow] Procedure of Init action.
      */
    new BackgroundTask(() => {
      start()

      /**
        * [External Command] Gets riggings and params from derrick.
        */
      val paramsTry = Try {
        Derrick.get_riggings_and_params(option.getDerrickExecPath, projOption.getWorkDir)
      }
      paramsTry match {
        case Success(resp) =>
          if (resp.status == "success")
            doneFrom("<derrick init> succeeded and no parameter was needed.")
          else if (resp.code != "multi_rigging_satisfied" && resp.code != "params_not_provided")
            failFrom(s"<derrick init> failed with an unrecognized error. ${resp.code} - ${resp.message}")
        case Failure(e) =>
          failFrom(s"unexpected error in getting riggings and params from derrick: ${e.getMessage}")
      }
      if (!isRunning) return

      /**
        * [UI Task] Popups a dialog to generate configurations for this action.
        */
      var rigging: String = null
      var params: Map[String, String] = null
      new WaitableUITask(() => {
        // Failure has been handled already.
        val resp = paramsTry.get
        val configDialog = new DerrickConfigForm(project, action, resp.riggingsAndParamsInJava)
        configDialog.show()
        // return if the dialog does not exist with OK.
        if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE)
          cancel()
        rigging = configDialog.getRigging
        params = configDialog.getParams.asScala.toMap
      }).run
      if (!isRunning) return

      /**
        * [External Command] Invokes derrick init.
        */
      assert(rigging != null && params != null)
      val initTry = Try {
        val derrick = new Derrick(option.getDerrickExecPath, projOption.getWorkDir)
        derrick.init(rigging, params)
      }
      initTry match {
        case Success(resp) =>
          if (resp.status == "success") done()
          else failFrom(s"<derrick init> failed in generating Dockerfile and other template files: ${resp.code} - ${resp.message}")
        case Failure(e) =>
          failFrom(s"unexpected error in invoking derrick init with user-defined parameters: ${e.getMessage}")
      }
    }).run
  }
}
