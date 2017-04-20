package services

import javax.inject.{Inject, Named, Singleton}

import actors.TcpDisplayUpdater
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class MessageUpdaterNetwork @Inject()(@Named("tcp-display-updater") tcpDisplayUpdater: ActorRef) extends MessageUpdater {

  override def isDeviceConnected: Future[Boolean] = {
    implicit val timeout = Timeout(1.second)
    (tcpDisplayUpdater ? TcpDisplayUpdater.IsDeviceConnected).map(_.asInstanceOf[Boolean])
  }

  override def getMessage: Future[String] = ???

  override def setMessage(msg: String): Future[Unit] = Future.successful {
    tcpDisplayUpdater ! TcpDisplayUpdater.Update(msg)
  }
}
