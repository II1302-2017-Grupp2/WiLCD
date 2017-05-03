package services

import models.{Id, Message, User, WithId}

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.MessageUpdater.DeleteResult

trait MessageUpdater {
  def isDeviceConnected: Future[Boolean]
  final def setMessage(user: Id[User], msg: String): Future[Unit] = scheduleMessage(Message(user, msg)).map(_ => ())
  def scheduleMessage(msg: Message): Future[WithId[Message]]
  def deleteMessage(user: Id[User], msg: Id[Message]): Future[DeleteResult]
  def getScheduledMessages: Future[Seq[WithId[Message]]]
  def getNextMessage: Future[Option[WithId[Message]]]
  def getMessage: Future[Option[WithId[Message]]]
}

object MessageUpdater {
  sealed trait DeleteResult
  object DeleteResult {
    case object Success extends DeleteResult
    case object NoPermission extends DeleteResult
    case object Archived extends DeleteResult
  }
}
