package actors

import java.time.Instant
import javax.inject.{Inject, Named}

import actors.DbMessageFetcher.{CurrentMessage, Refresh}
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.pattern.pipe
import models.{Message, WithId}
import services.MessageUpdater
import utils.Utils._

class DbMessageFetcher @Inject()(@Named("tcp-display-updater") updater: ActorRef, messageUpdater: MessageUpdater) extends Actor with ActorLogging {

  import context.dispatcher

  private var lastMessage: Option[WithId[Message]] = None
  private var nextRefresh: Option[Cancellable] = None

  //  context.system.scheduler.schedule(0.seconds, 1.second, self, Refresh)
  self ! Refresh

  override def receive: Receive = {
    case Refresh =>
      nextRefresh.foreach(_.cancel())
      messageUpdater.getMessage.zip(messageUpdater.getNextMessage).map(CurrentMessage.tupled) pipeTo self
    case CurrentMessage(newMessage, next) =>
      if (newMessage != lastMessage) {
        updater ! TcpDisplayUpdater.Update(newMessage.map(_.message).getOrElse(""))
        lastMessage = newMessage
      }
      val nextRefreshDelay =
        optionMin(
          newMessage.flatMap(_.displayUntil),
          next.map(_.displayFrom)
        ).map(java.time.Duration.between(Instant.now(), _))
      log.info(s"Next scheduled refresh: ${nextRefreshDelay.map(_.getSeconds + " seconds")}")
      nextRefresh.foreach(_.cancel())
      nextRefresh = nextRefreshDelay.map(context.system.scheduler.scheduleOnce(_, self, Refresh))
  }
}

object DbMessageFetcher {

  case class CurrentMessage(msg: Option[WithId[Message]], next: Option[WithId[Message]])

  object Refresh

}
