package xyz.ihac.intellij.plugin.derrick.common

import xyz.ihac.intellij.plugin.derrick.util.ImageNameFormat

class Image(rawName: String, val uuid: String) {
  val (url, username, name, tag) = {
    val imageRegex = raw"(([^/]+)/)?(\w+)/(\w+):(\w+)".r
    rawName match {
      case imageRegex(_, url, username, name, tag) =>
        if (url != null) (url, username, name, tag)
        else ("registry.hub.docker.com", username, name, tag)
      case _ => throw new IllegalArgumentException("cannot parse image name: %s".format(rawName))
    }
  }

  override def toString: String = "%s:%s".format(name, tag)
}

object Image {
  implicit def toImage(imageName: String): Image = new Image(imageName, null)
}
