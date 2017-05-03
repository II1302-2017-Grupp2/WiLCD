package services

import javax.inject.{Inject, Named}

import actors.{DbMessageFetcher, TcpDisplayUpdater}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import models.PgProfile.api._
import models.{Message, Messages, PgProfile, WithId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.concurrent.duration._

class MessageUpdaterDatabase @Inject()(@Named("db-message-fetcher") dbMessageFetcher: ActorRef,
                                       @Named("tcp-display-updater") tcpDisplayUpdater: ActorRef,
                                       val dbConfigProvider: DatabaseConfigProvider)
  extends MessageUpdater with HasDatabaseConfigProvider[PgProfile] {

  override def isDeviceConnected: Future[Boolean] = {
    implicit val timeout = Timeout(1.second)
    (tcpDisplayUpdater ? TcpDisplayUpdater.IsDeviceConnected).mapTo[Boolean]
  }

  override def scheduleMessage(msg: Message): Future[WithId[Message]] =
    for {
      created <- db.run(Messages.create(msg))
    } yield {
      dbMessageFetcher ! DbMessageFetcher.Refresh
      created
    }

  override def getMessage: Future[Option[WithId[Message]]] = db.run(for {
    _ <- Messages.updateReoccurring()
    message <- Messages.currentMessage.result.headOption
  } yield message)

  override def getNextMessage: Future[Option[WithId[Message]]] = db.run(for {
    _ <- Messages.updateReoccurring()
    message <- Messages.nextMessage.result.headOption
  } yield message)

  override def getScheduledMessages: Future[Seq[WithId[Message]]] = db.run(for {
    _ <- Messages.updateReoccurring()
    messages <- Messages.allMessages.result
  } yield messages)
}
