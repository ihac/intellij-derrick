package xyz.ihac.intellij.plugin.derrick.docker

import java.io.File

import com.github.dockerjava.api.model.BuildResponseItem
import com.github.dockerjava.core.command.BuildImageResultCallback
import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientBuilder}
import xyz.ihac.intellij.plugin.derrick.logging.Logger

import scala.collection.JavaConverters._

/** Implements Docker wrapper library by invoking SDK API.
  *
  * @param unixSock path to unix socket.
  *                 "unix:///var/run/docker.sock" by default.
  */
class Docker(unixSock: String) {
  val config = DefaultDockerClientConfig.createDefaultConfigBuilder
    .withDockerHost(unixSock)
  val client = DockerClientBuilder.getInstance(config).build

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
}
