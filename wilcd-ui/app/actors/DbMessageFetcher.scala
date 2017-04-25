package actors

import javax.inject.{Inject, Named}

import actors.DbMessageFetcher.{CurrentMessage, Refresh}
import akka.actor.{Actor, ActorRef}
import services.MessageUpdater

import scala.concurrent.duration._

class DbMessageFetcher @Inject() (@Named("tcp-display-updater") updater: ActorRef, messageUpdater: MessageUpdater) extends Actor {
  import context.dispatcher

  private var lastMessage: String = ""

  context.system.scheduler.schedule(0.seconds, 1.second, self, Refresh)

  override def receive: Receive = {
    case Refresh =>
      self ! messageUpdater.getMessage.map(CurrentMessage)
    case CurrentMessage(newMessage) =>
      if (newMessage != lastMessage) {
        updater ! TcpDisplayUpdater.Update(newMessage)
        lastMessage = newMessage
      }
  }
}

object DbMessageFetcher {
  object Refresh
  case class CurrentMessage(msg: String)
}
