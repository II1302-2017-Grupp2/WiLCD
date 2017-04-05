package services

import javax.inject.{Inject, Named, Singleton}

import actors.TcpDisplayUpdater
import akka.actor.ActorRef

import scala.concurrent.Future

@Singleton
class MessageUpdaterNetwork @Inject()(@Named("tcp-display-updater") tcpDisplayUpdater: ActorRef) extends MessageUpdater {

  override def getMessage: Future[String] = ???

  override def setMessage(msg: String): Future[Unit] = Future.successful {
    tcpDisplayUpdater ! TcpDisplayUpdater.Update(msg)
  }
}
