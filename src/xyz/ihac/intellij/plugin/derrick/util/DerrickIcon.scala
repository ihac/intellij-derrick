package xyz.ihac.intellij.plugin.derrick.util

import com.intellij.openapi.util.IconLoader

object DerrickIcon {
  val STATE_SUCCESS = IconLoader.getIcon("/icons/success_16x16.png")
  val STATE_FAIL = IconLoader.getIcon("/icons/fail_16x16.png")

  val ACTION_INIT = IconLoader.getIcon("/icons/build_16x16.png")
  val ACTION_DEPLOY = IconLoader.getIcon("/icons/deploy_v2_16x16.png")

  val TOOL_DOCKER = IconLoader.getIcon("/icons/docker_16x16.png")
  val TOOL_KUBERNETES = IconLoader.getIcon("/icons/kubernetes_16x16.png")
}
