package services
import javax.inject.{Inject, Singleton}

import models.{Id, Message, User, WithId}
import play.api.Logger

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class MessageUpdaterNoop @Inject() extends MessageUpdater {
  private lazy val logger = Logger(getClass)
  private val lock = new Object

  private var message: Option[Message] = None


  override def isDeviceConnected: Future[Boolean] = Future.successful(true)

  override def getMessage: Future[Option[WithId[Message]]] =
    getScheduledMessages.map(_.headOption)

  override def deleteMessage(user: Id[User], msg: Id[Message]): Future[MessageUpdater.DeleteResult] = ???

  override def getNextMessage: Future[Option[WithId[Message]]] = Future.successful(None)

  override def scheduleMessage(msg: Message): Future[WithId[Message]] = {
    Future {
      logger.warn(s"Updated message: $msg")
      lock.synchronized {
        message = Some(msg)
      }
      WithId(Id[Message](-1), msg)
    }
  }

  override def getScheduledMessages: Future[Seq[WithId[Message]]] = Future {
    lock.synchronized {
      message.map(WithId(Id[Message](-1), _)).toSeq
    }
  }
}
