package xyz.ihac.intellij.plugin.derrick.util

import xyz.ihac.intellij.plugin.derrick.docker.DockerRegistryConfiguration

trait Validatable[T] {
  def verify(e: T): Boolean
}

object NonEmpty extends Validatable[String] {
  override def verify(e: String): Boolean = e != null && e != ""
}

class NotEqual(val m: String) extends Validatable[String] {
  override def verify(e: String): Boolean = m != e
}

object NotEqual {
  def apply(m: String): NotEqual = new NotEqual(m)
}

object UrlFormat extends Validatable[String] {
  override def verify(e: String): Boolean = {
    val url = raw"\w+(\.\w+)+(:\d+)?".r
    e match {
      case url(_*) => true
      case _ => false
    }
  }
}
object EmailFormat extends Validatable[String] {
  override def verify(e: String): Boolean = {
    val email = raw"(\w+)@(.*)".r
    e match {
      case email(_, domain) => UrlFormat.verify(domain)
      case _ => false
    }
  }
}

object ImageNameFormat extends Validatable[String] {
  override def verify(e: String): Boolean = {
    val image = raw"(([^/]+)/)?(\w+)/([-a-z0-9]+):([a-zA-Z0-9.]+)".r
    e match {
      case image(prefix, url, _, _, _) => prefix == null || UrlFormat.verify(url)
      case _ => false
    }
  }
}

class ImageNameMatchRegistry(val registry: DockerRegistryConfiguration) extends Validatable[String] {
  override def verify(e: String): Boolean = {
    val image = raw"(([^/]+)/)?(\w+)/([-a-z0-9]+):([a-zA-Z0-9.]+)".r
    if (registry == null) false
    else e match {
      case image(prefix, url, user, name, tag) =>
        user == registry.getUsername &&
          (prefix == null || url == registry.getUrl)
      case _ => false
    }
  }
}

object ImageNameMatchRegistry {
  def apply(registry: DockerRegistryConfiguration): ImageNameMatchRegistry = new ImageNameMatchRegistry(registry)
}
