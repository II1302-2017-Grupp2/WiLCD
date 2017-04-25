package models

import java.time.Instant

import models.PgProfile.api._
import slick.dbio.Effect
import slick.lifted.ProvenShape
import play.api.libs.concurrent.Execution.Implicits.defaultContext

case class Message(createdBy: Id[User], createdAt: Instant,
                   displayFrom: Instant, displayUntil: Option[Instant],
                   message: String) extends HasId {
  override type IdType = Long
}

class Messages(tag: Tag) extends Table[WithId[Message]](tag, "messages") {
  def id = column[Id[Message]]("id", O.PrimaryKey)

  def createdBy = column[Id[User]]("created_by")

  def createdAt = column[Instant]("created_at")

  def displayFrom = column[Instant]("display_from")

  def displayUntil = column[Option[Instant]]("display_until")

  def message = column[String]("message")

  def all = (createdBy, createdAt, displayFrom, displayUntil, message) <> (Message.tupled, Message.unapply _)

  def * : ProvenShape[WithId[Message]] = (id, all) <> ((WithId.apply[Message] _).tupled, WithId.unapply[Message])
}

object Messages {
  private[models] def tq = TableQuery[Messages]

  def create(message: String): DBIO[Unit] =
    (tq.map(_.all) += Message(
      createdBy = Id[User](1),
      createdAt = Instant.now(),
      displayFrom = Instant.now(),
      displayUntil = None,
      message = message
    )).map(_ => ())

  def currentMessage: DBIO[Option[WithId[Message]]] =
    tq
      .filter(_.displayFrom < Instant.now())
      .filter(_.displayUntil.map(_ > Instant.now()) getOrElse true)
      .sorted(_.displayFrom.desc)
      .take(1)
      .result.headOption
}
