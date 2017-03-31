package services

import scala.concurrent.Future

trait MessageUpdater {
  def setMessage(msg: String): Future[Unit]
  def getMessage: Future[String]
}
