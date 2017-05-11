package models

import java.time.temporal.TemporalAmount
import java.time.{Instant, Period}

import models.Message.Occurrence
import models.PgProfile.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.MessageUpdater.DeleteResult
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

    def delta(occurrence: Value): TemporalAmount = occurrence match {
      case Once => throw new UnsupportedOperationException
      case Daily => Period.ofDays(1)
      case Weekly => Period.ofWeeks(1)
      case Monthly => Period.ofMonths(1)
      case Yearly => Period.ofYears(1)
    }
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

  def delete(user: Id[User], msgId: Id[Message]): DBIO[DeleteResult] = {
    val msgQuery =
      allMessages
        .filter(_.id === msgId)
    msgQuery
      .forUpdate
      .result.headOption.flatMap {
      case None =>
        DBIO.successful(DeleteResult.Success)
      case Some(msg) if msg.createdBy != user =>
        DBIO.successful(DeleteResult.NoPermission)
      case Some(msg) =>
        msgQuery.delete.map(_ => DeleteResult.Success)
    }.transactionally
  }

  def updateReoccurring(): DBIO[Unit] =
    (for {
      pastMessages <- allMessages
        .filter(_.displayFrom < Instant.now())
        .filter(_.occurrence =!= Message.Occurrence.Once)
        .forUpdate
        .result
      _ <- DBIO.seq(pastMessages.map(message => for {
        _ <- tq.filter(_.id === message.id).map(_.occurrence).update(Message.Occurrence.Once)
        _ <- tq.map(_.all) += message.value.copy(
          displayFrom = message.displayFrom.plus(Occurrence.delta(message.occurrence)),
          displayUntil = message.displayUntil.map(_.plus(Occurrence.delta(message.occurrence)))
        )
      } yield ()): _*)
    } yield ()).transactionally

  def currentMessage: Query[Messages, WithId[Message], Seq] =
    notYetDiscardedMessages
      .filter(_.displayFrom <= Instant.now())
      .take(1)

  def notYetDiscardedMessages: Query[Messages, WithId[Message], Seq] =
    allMessages
      .filter(_.displayUntil.map(_ > Instant.now()) getOrElse true)

  def allMessages: Query[Messages, WithId[Message], Seq] =
    tq
      .sorted(_.displayFrom.desc)

  private[models] def tq = TableQuery[Messages]

  def nextMessage: Query[Messages, WithId[Message], Seq] =
    futureMessages
      .take(1)

  def futureMessages: Query[Messages, WithId[Message], Seq] =
    notYetDiscardedMessages
      .filter(_.displayFrom > Instant.now())
      .sorted(_.displayFrom.asc)
}
