package xyz.ihac.intellij.plugin.derrick.util

import java.util.concurrent.{Callable, Future}

import com.intellij.openapi.application.ApplicationManager

class BackgroundTask[T](val taskFunc: () => T) {
  def run: Future[T] =
    ApplicationManager.getApplication.executeOnPooledThread(new Callable[T] {
      override def call(): T = taskFunc()
    })
}

class UITask(val taskFunc: () => Unit) {
  def run: Unit =
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = taskFunc()
    })
}

class WaitableUITask(val taskFunc: () => Unit) {
  def run: Unit =
    ApplicationManager.getApplication.invokeAndWait(new Runnable {
      override def run(): Unit = taskFunc()
    })
}
