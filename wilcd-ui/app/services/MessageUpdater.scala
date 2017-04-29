package services

import models.{Id, User}

import scala.concurrent.Future

trait MessageUpdater {
  def isDeviceConnected: Future[Boolean]
  def setMessage(user: Id[User], msg: String): Future[Unit]
  def getMessage: Future[String]
}
