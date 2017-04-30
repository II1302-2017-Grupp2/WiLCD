package services

import javax.inject.{Inject, Named}

import actors.DbMessageFetcher
import akka.actor.ActorRef
import models.{Message, Messages, PgProfile, WithId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import PgProfile.api._

import scala.concurrent.Future

class MessageUpdaterDatabase @Inject()(@Named("db-message-fetcher") dbMessageFetcher: ActorRef, val dbConfigProvider: DatabaseConfigProvider) extends MessageUpdater with HasDatabaseConfigProvider[PgProfile] {
  override def isDeviceConnected: Future[Boolean] = Future.successful(true)

  override def scheduleMessage(msg: Message): Future[Unit] = db.run(Messages.create(msg)).map(_ => dbMessageFetcher ! DbMessageFetcher.Refresh)

  override def getMessage: Future[String] = db.run(Messages.currentMessage.result.headOption.map(_.map(_.value.message).getOrElse("")))

  override def getScheduledMessages: Future[Seq[WithId[Message]]] = db.run(Messages.allMessages.result)
}
