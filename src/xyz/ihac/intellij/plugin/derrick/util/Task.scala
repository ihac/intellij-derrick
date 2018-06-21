package xyz.ihac.intellij.plugin.derrick.util

import com.intellij.openapi.application.ApplicationManager

trait Task {
  def run: Unit
}

class ExternalTask(val taskFunc: () => Unit) extends Task {
  override def run: Unit =
    ApplicationManager.getApplication.executeOnPooledThread(new Runnable {
      override def run(): Unit = taskFunc
    })
}

class UITask(val taskFunc: () => Unit) extends Task {
  override def run: Unit =
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = taskFunc
    })
}
