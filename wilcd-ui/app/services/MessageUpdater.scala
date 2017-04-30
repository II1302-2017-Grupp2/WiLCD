package services

import models.{Id, Message, User, WithId}

import scala.concurrent.Future

trait MessageUpdater {
  def isDeviceConnected: Future[Boolean]
  final def setMessage(user: Id[User], msg: String): Future[Unit] = scheduleMessage(Message(user, msg))
  def scheduleMessage(msg: Message): Future[Unit]
  def getScheduledMessages: Future[Seq[WithId[Message]]]
  def getMessage: Future[String]
}
