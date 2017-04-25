package services

import javax.inject.Inject

import models.{Messages, PgProfile}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class MessageUpdaterDatabase @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends MessageUpdater with HasDatabaseConfigProvider[PgProfile] {
  override def isDeviceConnected: Future[Boolean] = Future.successful(true)

  override def setMessage(msg: String): Future[Unit] = db.run(Messages.create(msg))

  override def getMessage: Future[String] = db.run(Messages.currentMessage.map(_.map(_.value.message).getOrElse("")))
}
