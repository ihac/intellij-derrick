package xyz.ihac.intellij.plugin.derrick.action

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindowManager
import xyz.ihac.intellij.plugin.derrick.kubernetes.{K8sClusterConfiguration, Kubernetes}
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.util.{BackgroundTask, WaitableUITask}
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

import scala.util.{Failure, Success, Try}

class DeployAction extends AnAction with ActionHelper {
  val action = "Deploy"

  override def actionPerformed(e: AnActionEvent): Unit = {
    /**
      * Prepares for deploy action.
      */
    val project = e.getProject
    val eventLog = ToolWindowManager.getInstance(project).getToolWindow("Event Log")
    if (!eventLog.isVisible)
      eventLog.show(null)

    val option = ServiceManager.getService(classOf[DerrickOptionProvider])
    val projOption = ServiceManager.getService(project, classOf[DerrickProjectOptionProvider])

    /**
      * [Flow] Procedure of Deploy action.
      */
    new BackgroundTask(() => {
      start()

      /**
        * [UI Task] Popups a dialog to generate configurations for this action.
        */
      var cluster: K8sClusterConfiguration = null
      var deployment: String = null
      new WaitableUITask(() => {
        val configDialog = new DerrickConfigForm(project, action)
        configDialog.show()
        // return if the dialog does not exist with OK.
        if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE)
          cancel()
        cluster = configDialog.getK8sCluster
        deployment = configDialog.getDeploymentYaml
      }).run
      if (!isRunning) return

      /**
        * [External API] Deploys application to cloud.
        */
      assert(cluster != null && deployment != null)
      log(s"start to deploy application to cloud <${cluster}>", INFO_LEVEL)
      val deployTry = Try {
        val k8s =
          if (cluster.getCtype == K8sClusterConfiguration.STANDARD_K8S_CLUSTER)
            new Kubernetes(cluster.getKubeconfig, true)
          else
            new Kubernetes(cluster.getKubeconfig, false)
        k8s.deployApp(deployment)
      }
      deployTry match {
        case Success(_) =>
          doneFrom(s"succeed in deploying application to cloud <${cluster}>")
        case Failure(e) =>
          failFrom(s"unexpected error in deploying application to cloud <${cluster}>: ${e.getMessage}")
      }
    }).run
  }
}
