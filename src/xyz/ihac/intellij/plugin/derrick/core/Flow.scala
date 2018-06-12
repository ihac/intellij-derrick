package xyz.ihac.intellij.plugin.derrick.core

import java.io._

import com.github.dockerjava.api.model.{BuildResponseItem, PushResponseItem}
import com.github.dockerjava.core.command.{BuildImageResultCallback, PushImageResultCallback}
import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientBuilder}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.{CoreV1Api, ExtensionsV1beta1Api}
import io.kubernetes.client.models.{ExtensionsV1beta1Deployment, V1ReplicationController, V1Service}
import io.kubernetes.client.util.{ClientBuilder, KubeConfig, Yaml}
import xyz.ihac.intellij.plugin.derrick.common._
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.ui.DerrickConfigForm
import xyz.ihac.intellij.plugin.derrick.{DerrickOptionProvider, DerrickProjectOptionProvider}

import scala.collection.JavaConverters._
import scala.io.Source.fromFile

class Flow(val action: String, val option: DerrickOptionProvider, val projOption: DerrickProjectOptionProvider) {
  def deploy2Cloud(config: Config): Unit = {
    try {
      /*
       * create kubernetes API client.
       */
      val kbcfg_str = config("kubeconfig").toString
      val reader =
        if ((new File(kbcfg_str)).exists) new FileReader(kbcfg_str);
        else new StringReader(kbcfg_str);
      val kubecfg = KubeConfig.loadKubeConfig(reader);
      val client = ClientBuilder.kubeconfig(kubecfg).build()

      Configuration.setDefaultApiClient(client)
      val v1Api = new CoreV1Api
      val extV1Beta1Api = new ExtensionsV1beta1Api

      /*
       * load k8s resource class
       */
      Yaml.addModelMap("v1", "ReplicationController", classOf[V1ReplicationController])
      Yaml.addModelMap("v1", "Service", classOf[V1Service])
      Yaml.addModelMap("extensions/v1beta1", "Deployment", classOf[ExtensionsV1beta1Deployment])

      /*
       * deploy kubernetes resources from deployment spec (yaml).
       */
      val contents = fromFile(config("deploymentYaml").toString).mkString
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
              Logger.error(action, "unknown file format in %s".format(config("deploymentYaml")))
              throw new RuntimeException("plugin does not support %s for kubernetes deployment".format(config("deploymentYaml")))
            }
          }
        }
      })
    } catch {
      // TODO: shall we handle some particular exceptions here?
      case e: Exception => e.printStackTrace(); throw e
    }
  }

  def push2Remote(config: Config, image: Image, option: DerrickOptionProvider): Unit = {
    try {
      /*
       * create docker client.
       */
      val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder
        .withDockerHost(option.getDockerExecPath)
        .withRegistryUrl(config("url").toString)
        .withRegistryUsername(config("username").toString)
        .withRegistryPassword(config("password").toString)
        .withRegistryEmail(config("email").toString)
      val dockerClient = DockerClientBuilder.getInstance(dockerConfig).build

      /*
       * callback function for docker push command.
       */
      val callback = new PushImageResultCallback() {
        override def onNext(item: PushResponseItem): Unit = {
          super.onNext(item)
          Logger.info(action, "[Docker] %s".format(
            // TODO: deprecated.
            (item.getStatus, item.getProgress) match {
              case (null, null) => "Done"
              case (status, null) => status
              case (status, progress) => status + progress
            }
          ))
        }
      }
      dockerClient.pushImageCmd(image.toString).exec(callback)
    } catch {
      // TODO: exception handling.
      case e: Exception => throw e
    }
  }

  def runContainer(image: Image, config: Config): App = {
    try {
      /*
       * create docker client.
       */
      val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder
        .withDockerHost(option.getDockerExecPath)
      val dockerClient = DockerClientBuilder.getInstance(dockerConfig).build

      /*
       * invoke docker start container method.
       */
      val container = dockerClient.createContainerCmd(image.toString).exec
      dockerClient.startContainerCmd(container.getId).exec

      new App(container.getId())
    } catch {
      case e: Exception => throw e
    }
  }

  def buildImage(spec: Spec, config: Config): Image = {
    try {
      Logger.info(action, "start to build image...")

      /*
       * create docker client.
       */
      val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder
        .withDockerHost(option.getDockerExecPath)
      val dockerClient = DockerClientBuilder.getInstance(dockerConfig).build

      /*
       * callback function for image build command.
       */
      val callback = new BuildImageResultCallback() {
        override def onNext(item: BuildResponseItem): Unit = {
          super.onNext(item)
          if (item.getStream != null)
            Logger.info(action, "[Docker] %s".format(item.getStream.trim))
        }
      }

      /*
       * invoke docker build method.
       */
      val imageName = config("imageId").toString
      val dir = new File(projOption.getWorkDir)
      dockerClient.buildImageCmd(dir).withTags(Set(imageName).asJava).exec(callback);

      Logger.info(action, "succeed to build image.")
      val Array(name, tag) = imageName.split(":")
      new Image(name, tag, "")
    } catch {
      case e: IllegalArgumentException => {
        Logger.error(action, "Dockerfile does not exist.<br> You may call init first or change your work directory.</html>")
        throw e
      }
      case e: Exception => throw e
    }
  }

  def generateSpecs(project: Project, config: Config) : Spec = null

  def initConfig(project: Project): Config = {
    /*
     * popup a dialog for generating configuration.
     */
    val configDialog = new DerrickConfigForm(project, action)
    configDialog.show()
    // return null if the dialog does not exist with OK.
    if (configDialog.getExitCode != DialogWrapper.OK_EXIT_CODE)
      null
    else {
      val (isRebuild, imageId, deploymentYaml, rigging, rawParams, registry, cluster) = action match {
        case "Init" => (null, null, null, configDialog.getRigging, configDialog.getParams.asScala, null, null)
        case "Serve" => (configDialog.getIsRebuild, configDialog.getImageId, null, null, null, null, null)
        case "Push" => (configDialog.getIsRebuild, configDialog.getImageId, null, null, null, configDialog.getDockerRegistry, null)
        case "Deploy" => (configDialog.getIsRebuild, configDialog.getImageId, configDialog.getDeploymentYaml, null, null, null, configDialog.getK8sCluster)
      }

      val params =
        if (rawParams != null) rawParams.map {
          pair => (pair._1, MyString(pair._2).asInstanceOf[MyValue])
        }.toMap
        else null
      new Config(MyObject(Map[String, MyValue](
        "imageId" -> MyString(imageId),
        "isRebuild" -> MyBoolean(isRebuild),
        "deploymentYaml" -> MyString(deploymentYaml),
        "rigging" -> MyString(rigging),
        "params" -> MyObject(params),
        "url" -> MyString(if (registry != null) registry.getName else ""),
        "username" -> MyString(if (registry != null) registry.getUsername else ""),
        "password" -> MyString(if (registry != null) registry.getPassword else ""),
        "email" -> MyString(if (registry != null) registry.getEmail else ""),
        "kubeconfig" -> MyString(if (cluster != null) cluster.getKubeconfig else "")
      )))
    }
  }

}
