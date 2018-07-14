package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.derrick.Derrick
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.{ExternalTask, UITask}
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

import scala.collection.JavaConverters._

class InitAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    /**
      * Prepares for init action.
      */
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)
    Logger.info("Init", "init action start...")

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

    /**
      * [External Command] Gets riggings and params from derrick.
      */
    new ExternalTask(() => {
      try {
        val resp =
          Derrick.get_riggings_and_params(option.getDerrickExecPath, projOption.getWorkDir)
        // no parameter is needed for derrick init.
        if (resp.status == "success") {
          Logger.info("Init", "init action done.")
          return
        }

        /**
          * [UI Task] Popups a dialog to generate configurations for this action.
          */
        new UITask(() => {
          val configDialog = new DerrickConfigForm(project, "Init", resp.riggingsAndParamsInJava);
          configDialog.show();
          // return if the dialog does not exist with OK.
          if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE) {
            Logger.info("Init", "Init action cancelled.")
            return
          }

          /**
            * [External Command] Invokes derrick init.
            */
          new ExternalTask(() => {
            try {
              val (rigging, params) =
                (configDialog.getRigging, configDialog.getParams)
              val derrick = new Derrick(option.getDerrickExecPath, projOption.getWorkDir)
              val resp = derrick.init(rigging, params.asScala.toMap)
              if (resp.status == "success")
                Logger.info("Init", "init action done.")
              else
                Logger.info("Init", "init action failed when calling <derrick init> command: %s".format(resp.message))
            } catch {
              case e: Exception => {
                Logger.error("Init", "error in invoking derrick init with user-defined parameters: " + e.getMessage)
                Logger.info("Init", "init action failed.")
              }
            }
          }).run
        }).run
      } catch {
        case e: Exception => {
          Logger.error("Init", "error in getting riggings and params from derrick: " + e.getMessage)
          Logger.info("Init", "init action failed.")
        }
      }
    }).run
  }
}
