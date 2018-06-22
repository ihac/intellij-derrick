package xyz.ihac.intellij.plugin.derrick.kubernetes

import java.io.{FileReader, Reader, StringReader}

import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.{CoreV1Api, ExtensionsV1beta1Api}
import io.kubernetes.client.models.{ExtensionsV1beta1Deployment, V1ReplicationController, V1Service}
import io.kubernetes.client.util.{ClientBuilder, KubeConfig, Yaml}
import xyz.ihac.intellij.plugin.derrick.logging.Logger

import scala.io.Source.fromFile

// TODO: Throw exception

/** Implements Kubernetes wrapper library by invoking official Kubernetes java SDK.
  *
  * @param kubeconfig path to kubeconfig, or file content of kubeconfig.
  * @param isFile whether kubeconfig param is a file path.
  */
class Kubernetes(kubeconfig: String, isFile: Boolean) {
  val client = {
    val reader =
      if (isFile) new FileReader(kubeconfig)
      else new StringReader(kubeconfig)
    val config = KubeConfig.loadKubeConfig(reader)
    ClientBuilder.kubeconfig(config).build()
  }
  Configuration.setDefaultApiClient(client)
  val v1Api = new CoreV1Api
  val extV1Beta1Api = new ExtensionsV1beta1Api

  /**
    * Loads k8s resource classes.
    */
  Yaml.addModelMap("v1", "ReplicationController", classOf[V1ReplicationController])
  Yaml.addModelMap("v1", "Service", classOf[V1Service])
  Yaml.addModelMap("extensions/v1beta1", "Deployment", classOf[ExtensionsV1beta1Deployment])

  /** Deploys application to cloud.
    *
    * @param deployment path to deployment yaml file.
    */
  def deployApp(deployment: String): Unit = {
    val contents = fromFile(deployment).mkString
    contents.split("---") foreach (item => {
      if (item.length != 0) {
        Yaml.load(item) match {
          case rc: V1ReplicationController =>
            v1Api.createNamespacedReplicationController("default", rc, "true")
          case svc: V1Service =>
            v1Api.createNamespacedService("default", svc, "true")
          case dp: ExtensionsV1beta1Deployment =>
            extV1Beta1Api.createNamespacedDeployment("default", dp, "true")
          case _ => {
            Logger.error("DeployApp", "unknown file format in %s".format(deployment))
            throw new RuntimeException("plugin does not support %s for kubernetes deployment".format(deployment))
          }
        }
      }
    })
  }
}
