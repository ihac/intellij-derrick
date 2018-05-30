package xyz.ihac.intellij.plugin.derrick.addon

import java.nio.file.Paths

import com.intellij.execution.ExecutionException
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import xyz.ihac.intellij.plugin.derrick.util.Command
import collection.JavaConverters._

class Derrick(val workDir: String) {
  private def call(subcomm: String, param: List[String]): JsValue = {
    val newParams = subcomm::param:::List("--output", "json", "--silent")
    val comm =
        new Command(Paths.get(workDir), "derrick_mock", newParams:_*)
    try {
      comm.exec()
    } catch {
      case e: ExecutionException => throw e
    }
  }

  def init(rigging: String, params: Map[String, String]): JsValue = {
    try {
      if (rigging != null && params != null) {
        val params_str = "%s".format(Json.toJson(params).toString)
        call("init", List("--rigging", rigging, "--params", params_str))
      }
      else
        call("init", Nil)
    }
    catch {
      case e: Exception => throw e
    }
  }

}
object Derrick {
  def get_riggings_and_params(workDir: String): java.util.Map[String, java.util.List[java.util.Map[String, String]]] = {
    val derrick = new Derrick(workDir)
    val res = derrick.init(null, null)
    val status = (res \ "status").validate[String].get
    val code = (res \ "code").validate[String].get

    if (status == "error" &&
      (code == "params_not_provided" || code == "multi_rigging_satisfied")) {
      val riggings = (res \ "riggings").validate[List[JsObject]].get
      riggings.foldLeft(Map[String, java.util.List[java.util.Map[String, String]]]()) {
        (mp, obj) => {
          val name = (obj \ "name").validate[String].get
          val params = (obj \ "params").validate[List[Map[String, String]]].get.map { _.asJava }
          mp + (name -> params.asJava)
        }
      }.asJava
    }
    else
      throw new ExecutionException(res("code").as[String] + ": " + res("message").as[String])
  }
}
