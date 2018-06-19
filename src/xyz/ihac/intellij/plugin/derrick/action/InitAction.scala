package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.application.{ApplicationManager, ModalityState}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.core.Flow
import xyz.ihac.intellij.plugin.derrick.derrick.Derrick
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

import scala.collection.JavaConverters._

class InitAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    try {
      /*
       * preparation.
       */
      val project = e.getProject
      val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
      if (!eventLog.isVisible)
        eventLog.show(null)
      Logger.info("Init", "init action start...")

      val option = ServiceManager.getService(classOf[DerrickOptionProvider])
      val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])
      val flow = new Flow("Init", option, projOption)

      ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
        override def run(): Unit = {
          /*
           * get riggings and params from derrick.
           */
          val resp =
            Derrick.get_riggings_and_params(option.getDerrickExecPath, projOption.getWorkDir)
          ApplicationManager.getApplication.invokeLater(new Runnable {
            override def run(): Unit = {
              /*
               * popup a dialog for generating configuration.
               */
              val configDialog = new DerrickConfigForm(project, "Init", resp.riggingsAndParamsInJava);
              configDialog.show();
              // return null if the dialog does not exist with OK.
              if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE)
                null
              else {
                val (rigging, params) =
                  (configDialog.getRigging, configDialog.getParams)

                /*
                 * invoke derrick init method
                 */
                val derrick = new Derrick(option.getDerrickExecPath, projOption.getWorkDir)
                ApplicationManager.getApplication.executeOnPooledThread(new Runnable() {
                  override def run(): Unit = {
                    val resp = derrick.init(rigging, params.asScala.toMap)
                    if (resp.status == "success")
                      Logger.info("Init", "init action done.")
                    else
                      Logger.info("Init", "init action failed when calling <derrick init>.")
                  }
                })
              }

            }
          })
        }
      })
    } catch {
      case e: Exception => Logger.error("Init", "init action failed: %s.".format(e.getMessage));
      case _ => Logger.error("Init", "init action failed due to unknown error(s). Contact author for support.")
    }
  }
}
