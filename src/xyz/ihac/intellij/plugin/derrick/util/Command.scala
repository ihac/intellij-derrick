package xyz.ihac.intellij.plugin.derrick.util

import java.nio.file.Path

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import play.api.libs.json.{JsValue, Json}
import xyz.ihac.intellij.plugin.derrick.logging.Logger

import scala.collection.JavaConverters._

class Command(comm: String, args: String*) {
  val commLine = new GeneralCommandLine
  commLine.setExePath(comm)
  commLine.addParameters(args.asJava)

  def this(workDir: Path, comm: String, args: String*) = {
    this(comm, args:_*)
    commLine.setWorkDirectory(workDir.toFile)
  }

  def this(comm: String, arg: String) {
    this(comm, Seq(arg):_*)
  }

  def raw_exec(): String = {
    val output = ExecUtil.execAndGetOutput(commLine)
    if (output.getExitCode != 0) {
      Logger.error("Exec Command", "error in running command {%s} :".format(commLine) + output.getStderr)
      throw new ExecutionException("command {%s} exits with non-zero value".format(commLine))
    }
    else
      output.getStdout
  }

  @throws[ExecutionException]
  def exec(): JsValue = {
    // block
    val output = ExecUtil.execAndGetOutput(commLine)
    if (output.getExitCode != 0) {
      Logger.error("Exec Command", "error in running command {%s}: %s".format(commLine, output.getStderr))
      throw new ExecutionException("command {%s} exits with non-zero value".format(commLine))
    }
    else {
      try {
        Json.parse(output.getStdout)
      } catch {
        case e: Exception => {
          Logger.error("Json Parse", "cannot parse the output of command {%s}: %s".format(commLine, output.getStdout))
          throw e
        }
      }
    }
  }

  override def toString: String = commLine.toString
}
