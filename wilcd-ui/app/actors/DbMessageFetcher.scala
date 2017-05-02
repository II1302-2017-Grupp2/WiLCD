package actors

import java.time.Instant
import javax.inject.{Inject, Named}

import actors.DbMessageFetcher.{CurrentMessage, Refresh}
import akka.actor.{Actor, ActorRef, Cancellable}
import akka.pattern.pipe
import models.{Message, WithId}
import services.MessageUpdater
import utils.Utils._

import scala.concurrent.duration._

class DbMessageFetcher @Inject()(@Named("tcp-display-updater") updater: ActorRef, messageUpdater: MessageUpdater) extends Actor {

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
      nextRefresh.foreach(_.cancel())
      nextRefresh =
        optionMin(
          newMessage.flatMap(_.displayUntil),
          next.map(_.displayFrom)
        )
          .map(java.time.Duration.between(_, Instant.now()))
          .map(context.system.scheduler.scheduleOnce(_, self, Refresh))
  }
}

object DbMessageFetcher {

  object Refresh

  case class CurrentMessage(msg: Option[WithId[Message]], next: Option[WithId[Message]])

}
