package xyz.ihac.intellij.plugin.derrick.addon

import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.{CommonRequest, DefaultAcsClient}
import play.api.libs.json.{JsObject, Json}
import xyz.ihac.intellij.plugin.derrick.ui.AliyunCSCluster

import scala.collection.JavaConverters._

class AliyunCS(val region: String,
               val accessKeyID: String,
               val accessKeySecret: String) {
  private def raw_call(method: MethodType, url: String): String = {
    val profile = DefaultProfile.getProfile(region, accessKeyID, accessKeySecret);
    val client = new DefaultAcsClient(profile)
    val request = new CommonRequest

    request.setDomain("cs.aliyuncs.com")
    request.setVersion("2015-12-15")
    request.setMethod(method)
    request.setUriPattern(url)

    try {
      client.getCommonResponse(request).getData
    } catch {
      // TODO: exception handling
      case e: Exception => throw e
    }
  }

  def call(method: String, url: String): String = {
    val meth = method match {
      case "DELETE" => MethodType.DELETE
      case "GET" => MethodType.GET
      case "HEAD" => MethodType.HEAD
      case "OPTIONS" => MethodType.OPTIONS
      case "POST" => MethodType.POST
      case "PUT" => MethodType.PUT
    }
    raw_call(meth, url)
  }

  def describeClusters(): java.util.List[AliyunCSCluster] = {
    val res = raw_call(MethodType.GET, "/clusters")
    try {
      Json.parse(res).validate[List[JsObject]].get.map {
        obj => {
          val name = (obj \ "name").validate[String].get
          val id = (obj \ "cluster_id").validate[String].get
          val ctype = (obj \ "cluster_type").validate[String].get
          val masterUrl = (obj \ "outputs").validate[List[JsObject]].get.foldLeft("") {
            (url, o) =>
              if ((o \ "OutputKey").validate[String].get != "APIServerInternet") url
              else (o \ "OutputValue").validate[String].get
          }
          new AliyunCSCluster(name, id, ctype, masterUrl)
        }
      }.asJava
    } catch {
      // TODO: exception handling
      case e: Exception => throw e
    }
  }

  def describeClusterKubeConfig(clusterId: String): String = {
    // TODO: best practice when using assert?
    assert(clusterId != null && clusterId != "")
    val res = raw_call(MethodType.GET, "/k8s/%s/user_config".format(clusterId))
    try {
      Json.parse(res).validate[Map[String, String]].get.apply("config")
    } catch {
      // TODO: exception handling
      case e: Exception => throw e
    }
  }
}
