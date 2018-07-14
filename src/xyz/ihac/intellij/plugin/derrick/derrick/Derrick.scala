package xyz.ihac.intellij.plugin.derrick.derrick

import java.nio.file.Paths

import play.api.libs.json.{JsValue, Json}
import xyz.ihac.intellij.plugin.derrick.util.Command

class Derrick(val path: String, val workDir: String) {
  private def call(subcomm: String, param: List[String]): JsValue = {
    val newParams = subcomm::param:::List("--output", "json", "--silent")
    val comm = new Command(Paths.get(workDir), path, newParams:_*)

    comm.exec()
  }

  def version(): JsValue = {
    call("--version", Nil)
  }

  def init(rigging: String, params: Map[String, String]): DerrickInitCommandResponse = {
    val res =
      if (rigging != null && params != null) {
        val params_str = "%s".format(Json.toJson(params).toString)
        call("init", List("--rigging", rigging, "--params", params_str))
      }
      else
        call("init", Nil)
    new DerrickInitCommandResponse(res)
  }

}

object Derrick {
  def get_riggings_and_params(comm: String, workDir: String): DerrickInitCommandResponse = {
    val derrick = new Derrick(comm, workDir)
    derrick.init(null, null)
  }

  def verify(comm: String, workDir: String): Boolean = {
    val derrick = new Derrick(comm, workDir)
    try {
      val res = derrick.version()
      (res \ "version").validate[String].isSuccess
    }
    catch {
      case e: Exception => false
    }
  }
}
