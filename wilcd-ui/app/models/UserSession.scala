package models

import java.net.InetAddress
import java.time.Instant
import java.util.UUID

import models.PgProfile.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
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

  def userFk = foreignKey("user_fk", user, Users.tq)(_.id)

  override def * : ProvenShape[WithId[UserSession]] = (id, all) <> ((WithId.apply[UserSession] _).tupled, WithId.unapply[UserSession])
}

object UserSessions {
  private[models] def tq = TableQuery[UserSessions]

  def find(id: Id[UserSession]): Query[(Users, UserSessions), (WithId[User], WithId[UserSession]), Seq] =
    for {
      session <- tq
      if session.id === id
      if session.valid
      user <- session.userFk
    } yield (user, session)

  def create(user: Id[User], ip: InetAddress): DBIO[WithId[UserSession]] =
    tq.returning(tq) += WithId(Id[UserSession](UUID.randomUUID()), UserSession(user, ip))

  def destroy(id: Id[UserSession]): DBIO[Unit] =
    tq.filter(_.id === id).map(_.valid).update(false).map(_ => ())
}
