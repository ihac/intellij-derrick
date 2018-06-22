package xyz.ihac.intellij.plugin.derrick.docker

import java.io.File

import com.github.dockerjava.api.model.{BuildResponseItem, PushResponseItem}
import com.github.dockerjava.core.command.{BuildImageResultCallback, PushImageResultCallback}
import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientBuilder}
import DockerRegistryConfiguration
import xyz.ihac.intellij.plugin.derrick.logging.Logger
import xyz.ihac.intellij.plugin.derrick.ui.DockerRegistryConfigForm

import scala.collection.JavaConverters._

// TODO: Throw exception

/** Implements Docker wrapper library by invoking SDK API.
  *
  * @param unixSock path to unix socket.
  *                 "unix:///var/run/docker.sock" by default.
  * @param registry configuration of a registry.
  */
class Docker(unixSock: String, registry: DockerRegistryConfiguration) {
  val config = {
    val initConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
      .withDockerHost(unixSock)
    if (registry != null)
      initConfig.withRegistryUrl(registry.getUrl)
        .withRegistryUsername(registry.getUsername)
        .withRegistryPassword(registry.getPassword)
        .withRegistryEmail(registry.getEmail)
    else
      initConfig
  }
  val client = DockerClientBuilder.getInstance(config).build

  def this(unixSock: String) = {
    this(unixSock, null)
  }

  def this(unixSock: String,
           registryUrl: String,
           registryUsername: String,
           registryPassword: String,
           registryEmail: String) = {
    this(unixSock, new DockerRegistryConfiguration(
      "",
      registryUrl,
      registryUsername,
      registryPassword,
      registryEmail
    ))
  }

  /** Builds image from Dockerfile.
    *
    * @param workDir work directory where Dockerfile locates.
    * @param imageName name for the image to build, following the naming convention:
    *                  "[&lt;registry&gt;/]&lt;username&gt;/&lt;name&gt;:&lt;tag&gt;."
    * @return uuid of the image when succeed,
    *         null when failed.
    */
  def buildImage(workDir: String, imageName: String, logging: Boolean): String = {
    val callback =
      if (logging) new BuildImageResultCallback {
        override def onNext(item: BuildResponseItem): Unit = {
          super.onNext(item)
          if (item.getStream != null)
            Logger.info("BuildImage", "[Docker] %s".format(item.getStream.trim))
        }
      }
      else new BuildImageResultCallback
    client.buildImageCmd(new File(workDir))
      .withTags(Set(imageName).asJava)
      .exec(callback)
      .awaitImageId()
  }

  /** Creates and runs a container from existing image.
    *
    * @param imageName name of the image to run
    * @return container id when succeed,
    *         null when failed.
    */
  def runContainer(imageName: String): String = {
    val response = client.createContainerCmd(imageName).exec()
    client.startContainerCmd(response.getId).exec()
    response.getId
  }

  /** Pushs image to remote registry.
    *
    * @param imageName name of the image to push
    */
  def pushImage(imageName: String, logging: Boolean): Unit = {
    val callback =
      if (logging) new PushImageResultCallback {
        override def onNext(item: PushResponseItem): Unit = {
          super.onNext(item)
          Logger.info("PushImage", "[Docker] %s".format(item.getProgressDetail))
        }
      }
      else new PushImageResultCallback
    client.pushImageCmd(imageName).exec(callback).awaitSuccess()
  }
}
