package services

import javax.inject.{Inject, Named}

import actors.DbMessageFetcher.Refresh
import actors.TcpDisplayUpdater.NotifyBound
import actors.{DbMessageFetcher, TcpDisplayUpdater}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import models.PgProfile.api._
import models.{Id, Message, Messages, PgProfile, User, WithId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.concurrent.duration._

class MessageUpdaterDatabase @Inject()(@Named("db-message-fetcher") dbMessageFetcher: ActorRef,
                                       @Named("tcp-display-updater") tcpDisplayUpdater: ActorRef,
                                       val dbConfigProvider: DatabaseConfigProvider)
  extends MessageUpdater with HasDatabaseConfigProvider[PgProfile] {

  private implicit val akkaTimeout = Timeout(1.second)

  override def ready: Future[Unit] =
    (tcpDisplayUpdater ? NotifyBound).map(_ => ())

  override def isDeviceConnected: Future[Boolean] =
    (tcpDisplayUpdater ? TcpDisplayUpdater.IsDeviceConnected).mapTo[Boolean]

  override def deleteMessage(user: Id[User], msg: Id[Message]): Future[MessageUpdater.DeleteResult] =
    for {
      result <- db.run(Messages.delete(user, msg))
    } yield {
      dbMessageFetcher ! Refresh
      result
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
