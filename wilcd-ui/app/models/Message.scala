package models

import java.time.Instant

import models.PgProfile.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.lifted.ProvenShape

case class Message(createdBy: Id[User], message: String,
                   createdAt: Instant = Instant.now(),
                   displayFrom: Instant = Instant.now(), displayUntil: Option[Instant] = None) extends HasId {
  override type IdType = Long
}

class Messages(tag: Tag) extends IdTable[Message](tag, "messages") {
  def * : ProvenShape[WithId[Message]] = (id, all) <> ((WithId.apply[Message] _).tupled, WithId.unapply[Message])

  def all = (createdBy, message, createdAt, displayFrom, displayUntil) <> (Message.tupled, Message.unapply)

  def createdBy = column[Id[User]]("created_by")

  def message = column[String]("message")

  def createdAt = column[Instant]("created_at")

  def displayFrom = column[Instant]("display_from")

  def displayUntil = column[Option[Instant]]("display_until")
}

object Messages {
  def create(user: Id[User], message: String): DBIO[Unit] =
    (tq.map(_.all) += Message(
      createdBy = user,
      createdAt = Instant.now(),
      displayFrom = Instant.now(),
      displayUntil = None,
      message = message
    )).map(_ => ())

  private[models] def tq = TableQuery[Messages]

  def currentMessage: DBIO[Option[WithId[Message]]] =
    tq
      .filter(_.displayFrom < Instant.now())
      .filter(_.displayUntil.map(_ > Instant.now()) getOrElse true)
      .sorted(_.displayFrom.desc)
      .take(1)
      .result.headOption
}
