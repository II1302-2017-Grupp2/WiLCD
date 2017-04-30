package services

import models.{Id, Message, User}

import scala.concurrent.Future

trait MessageUpdater {
  def isDeviceConnected: Future[Boolean]
  final def setMessage(user: Id[User], msg: String): Future[Unit] = scheduleMessage(Message(user, msg))
  def scheduleMessage(msg: Message): Future[Unit]
  def getMessage: Future[String]
}
