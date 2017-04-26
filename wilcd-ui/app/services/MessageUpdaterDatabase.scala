package services

import javax.inject.{Inject, Named}

import actors.DbMessageFetcher
import akka.actor.ActorRef
import models.{Messages, PgProfile}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class MessageUpdaterDatabase @Inject()(@Named("db-message-fetcher") dbMessageFetcher: ActorRef, val dbConfigProvider: DatabaseConfigProvider) extends MessageUpdater with HasDatabaseConfigProvider[PgProfile] {
  override def isDeviceConnected: Future[Boolean] = Future.successful(true)

  override def setMessage(msg: String): Future[Unit] = db.run(Messages.create(msg)).map(_ => dbMessageFetcher ! DbMessageFetcher.Refresh)

  override def getMessage: Future[String] = db.run(Messages.currentMessage.map(_.map(_.value.message).getOrElse("")))
}
