package services

import scala.concurrent.Future

trait MessageUpdater {
  def isDeviceConnected: Future[Boolean]
  def setMessage(msg: String): Future[Unit]
  def getMessage: Future[String]
}
