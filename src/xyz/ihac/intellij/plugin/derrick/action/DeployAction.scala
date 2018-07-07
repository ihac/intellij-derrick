package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.kubernetes.{K8sClusterConfiguration, Kubernetes}
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.ExternalTask
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

class DeployAction extends AnAction {
  override def actionPerformed(e: AnActionEvent): Unit = {
    try {
      /**
        * Prepares for deploy action.
        */
      val project = e.getProject
      val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
      if (!eventLog.isVisible)
        eventLog.show(null)
      Logger.info("Deploy", "deploy action start...")

      val option = ServiceManager.getService(classOf[DerrickOptionProvider])
      val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

      /**
        * Popups a dialog to generate configurations for this action.
        * No need to use UITask since it's in Event Dispatch Thread.
        */
      val configDialog = new DerrickConfigForm(project, "Deploy")
      configDialog.show()
      // return if the dialog does not exist with OK.
      if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE) {
        Logger.info("Deploy", "deploy action cancelled.")
        return
      }
      val cluster = configDialog.getK8sCluster
      val deployment = configDialog.getDeploymentYaml

      /**
        * [External API] Deploys application to cloud.
        */
      new ExternalTask(() => {
        Logger.info("Deploy", "start to deploy application to cloud <%s>".format(cluster))
        val k8s =
          if (cluster.getCtype == K8sClusterConfiguration.STANDARD_K8S_CLUSTER)
            new Kubernetes(cluster.getKubeconfig, true)
          else
            new Kubernetes(cluster.getKubeconfig, false)
        k8s.deployApp(deployment)
        Logger.info("Deploy", "succeed in deploying application")
        Logger.info("Deploy", "deploy action done.")
      }).run

    } catch {
      case e: Exception => Logger.error("Deploy", "deploy action failed: %s".format(e.getMessage))
    }
  }
}
