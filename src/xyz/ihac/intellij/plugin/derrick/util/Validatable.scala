package xyz.ihac.intellij.plugin.derrick.util

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
  def set(m: String): NotEqual = new NotEqual(m)
}

object UrlFormat extends Validatable[String] {
  override def verify(e: String): Boolean = {
    val url = raw"\w+(\.\w+)+".r
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
    val image = raw"(([^/]+)/)?(\w+)/(\w+):(\w+)".r
    e match {
      case image(prefix, url, _, _, _) => prefix == null || UrlFormat.verify(url)
      case _ => false
    }
  }
}
