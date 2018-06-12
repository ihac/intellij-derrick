package xyz.ihac.intellij.plugin.derrick.common

class Image(val name: String, val tag: String, val uuid: String) {
  def this(name: String) = this(name, "latest", "")

  def getId: String = uuid
  override def toString: String = "%s:%s".format(name, tag)
}
