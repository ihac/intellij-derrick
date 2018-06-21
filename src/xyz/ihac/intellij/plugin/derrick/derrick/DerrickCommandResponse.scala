package xyz.ihac.intellij.plugin.derrick.derrick

import play.api.libs.json.{JsObject, JsValue}
import java.util.{Map => jMap, List => jList}
import scala.collection.JavaConverters._

abstract class DerrickCommandResponse(output: JsValue) {
  val status = (output \ "status").validate[String].get
  val code = (output \ "code").validate[String].get
  val message = (output \ "message").validate[String].get
}

class DerrickInitCommandResponse(output: JsValue) extends DerrickCommandResponse(output) {
  val riggingsAndParams: Map[String, List[Map[String, String]]] =
    if (status == "error" &&
      (code == "params_not_provided" || code == "multi_rigging_satisfied")) {
      val riggings = (output \ "riggings").validate[List[JsObject]].get
      riggings.foldLeft(Map[String, List[Map[String, String]]]()) {
        (mp, obj) => {
          val name = (obj \ "name").validate[String].get
          val params = (obj \ "params").validate[List[Map[String, String]]].get
          mp + (name -> params)
        }
      }
    }
    else null

  def riggingsAndParamsInJava: jMap[String, jList[jMap[String, String]]] =
    riggingsAndParams.map {
      case(k, v) => k -> v.map(_.asJava).asJava
    }.asJava
}
