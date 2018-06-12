package xyz.ihac.intellij.plugin.derrick.common

class Config(c: MyObject) {
  lazy val config: Map[String, MyValue] = c match {
    case MyObject(fields) => fields
    case _ => throw new RuntimeException("error in parsing config: " + c.toString)
  }

  def apply(key: String): MyValue = config(key)
}

sealed class MyValue

case class MyBoolean(value: Boolean) extends MyValue {
  override def toString: String = value.toString
  def toBoolean: Boolean = value
}

case class MyString(value: String) extends MyValue {
  override def toString: String = value
}

case class MyInt(value: Int) extends MyValue {
  override def toString: String = value.toString
  def toInt: Int = value
}

case class MyList(l: List[MyValue]) extends MyValue {
  override def toString: String = "[%s]".format(l mkString ", ")
}

case class MyObject(obj: Map[String, MyValue]) extends MyValue {
  def toScalaMap: Map[String, MyValue] = obj
  override def toString: String = "{\n%s\n}".format(
    obj map {
      case (key, value) => "\t" + key + ": " + value
    } mkString ",\n"
  )
}

