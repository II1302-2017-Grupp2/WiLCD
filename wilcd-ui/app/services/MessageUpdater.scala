package services

import models.{Id, Message, User, WithId}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

trait MessageUpdater {
  def isDeviceConnected: Future[Boolean]
  final def setMessage(user: Id[User], msg: String): Future[Unit] = scheduleMessage(Message(user, msg)).map(_ => ())
  def scheduleMessage(msg: Message): Future[WithId[Message]]
  def getScheduledMessages: Future[Seq[WithId[Message]]]
  def getMessage: Future[String]
}
