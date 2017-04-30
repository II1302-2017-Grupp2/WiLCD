package models

import java.time.Instant

import models.Message.Occurrence
import models.PgProfile.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.lifted.ProvenShape

case class Message(createdBy: Id[User], message: String,
                   createdAt: Instant = Instant.now(),
                   displayFrom: Instant = Instant.now(), displayUntil: Option[Instant] = None,
                   occurrence: Occurrence = Occurrence.Once) extends HasId {
  override type IdType = Long
}

object Message {
  type Occurrence = Occurrence.Value

  object Occurrence extends Enumeration {
    val Once, Daily, Weekly, Monthly, Yearly = Value
  }

}


class Messages(tag: Tag) extends IdTable[Message](tag, "messages") {
  def * : ProvenShape[WithId[Message]] = (id, all) <> ((WithId.apply[Message] _).tupled, WithId.unapply[Message])

  def all = (createdBy, message, createdAt, displayFrom, displayUntil, occurrence) <> ((Message.apply _).tupled, Message.unapply)

  def createdBy = column[Id[User]]("created_by")

  def message = column[String]("message")

  def createdAt = column[Instant]("created_at")

  def displayFrom = column[Instant]("display_from")

  def displayUntil = column[Option[Instant]]("display_until")

  def occurrence = column[Occurrence]("occurrence")
}

object Messages {
  def create(message: Message): DBIO[WithId[Message]] =
    (tq.map(_.all).returning(tq.map(_.id)) += message).map(WithId(_, message))

  def allMessages: Query[Messages, WithId[Message], Seq] =
    tq
      .sorted(_.displayFrom.desc)

  def currentMessage: Query[Messages, WithId[Message], Seq] =
    allMessages
      .filter(_.displayFrom < Instant.now())
      .filter(_.displayUntil.map(_ > Instant.now()) getOrElse true)
      .take(1)

  private[models] def tq = TableQuery[Messages]
}
