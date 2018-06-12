package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.addon.Derrick
import xyz.ihac.intellij.plugin.derrick.common.{MyObject, MyString}
import xyz.ihac.intellij.plugin.derrick.core.Flow
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

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

      /*
       * generate configuration for init action.
       */
      val config = flow.initConfig(project);
      if (config == null) {
        Logger.info("Init", "init action cancelled.")
        return
      }

      /*
       * invoke derrick init method
       */
      val derrick = new Derrick(option.getDerrickExecPath, projOption.getWorkDir)
      val rigging = config("rigging").asInstanceOf[MyString].toString
      val params = config("params").asInstanceOf[MyObject].toScalaMap.map {
        pair => (pair._1, pair._2.asInstanceOf[MyString].toString)
      }
      derrick.init(rigging, params)

      Logger.info("Init", "init action done.")
    } catch {
      case e: Exception => Logger.error("Init", "init action failed: %s.".format(e.getMessage));
      case _ => Logger.error("Init", "init action failed due to unknown error(s). Contact author for support.")
    }
  }
}
