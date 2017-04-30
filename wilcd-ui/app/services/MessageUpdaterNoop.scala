package services
import javax.inject.{Inject, Singleton}

import models.{Id, Message, User}
import play.api.Logger

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class MessageUpdaterNoop @Inject() extends MessageUpdater {
  private lazy val logger = Logger(getClass)
  private val lock = new Object

  private var message = ""


  override def isDeviceConnected: Future[Boolean] = Future.successful(true)

  override def getMessage: Future[String] = Future {
    lock.synchronized {
      message
    }
  }

  override def scheduleMessage(msg: Message): Future[Unit] = {
    Future {
      logger.warn(s"Updated message: $msg")
      lock.synchronized {
        message = msg.message
      }
    }
  }
}
