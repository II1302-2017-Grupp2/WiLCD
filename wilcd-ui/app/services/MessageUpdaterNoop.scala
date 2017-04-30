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

  override def getMessage: Future[String] =
    getScheduledMessages.map(_.headOption.map(_.message).getOrElse(""))

  override def scheduleMessage(msg: Message): Future[Unit] = {
    Future {
      logger.warn(s"Updated message: $msg")
      lock.synchronized {
        message = Some(msg)
      }
    }
  }

  override def getScheduledMessages: Future[Seq[WithId[Message]]] = Future {
    lock.synchronized {
      message.map(WithId(Id[Message](-1), _)).toSeq
    }
  }
}
