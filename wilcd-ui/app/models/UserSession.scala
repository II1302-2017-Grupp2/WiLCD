package models

import java.net.InetAddress
import java.time.Instant
import java.util.UUID

import PgProfile.api._
import slick.dbio.Effect
import slick.lifted.ProvenShape

case class UserSession(user: Id[User], ip: InetAddress, createdAt: Instant = Instant.now(), valid: Boolean = true) extends HasId {
  override type IdType = UUID
}

class UserSessions(tag: Tag) extends IdTable[UserSession](tag, "sessions") {
  def user = column[Id[User]]("user")
  def ip = column[InetAddress]("ip")
  def createdAt = column[Instant]("created_at")
  def valid = column[Boolean]("valid")

  def all = (user, ip, createdAt, valid) <> (UserSession.tupled, UserSession.unapply)

  override def * : ProvenShape[WithId[UserSession]] = (id, all) <> ((WithId.apply[UserSession] _).tupled, WithId.unapply[UserSession])
}

object UserSessions {
  private[models] def tq = TableQuery[UserSessions]

  def create(user: Id[User], ip: InetAddress): DBIOAction[WithId[UserSession], NoStream, Effect.Write] =
    tq.returning(tq) += WithId(Id[UserSession](UUID.randomUUID()), UserSession(user, ip))

  def destroy(session: Id[UserSession]): DBIO[Unit] = ???
}