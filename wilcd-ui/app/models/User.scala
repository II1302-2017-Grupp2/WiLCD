package models

import java.time.ZoneId

import akka.util.ByteString
import models.PgProfile.api._
import org.abstractj.kalium.NaCl.Sodium
import org.abstractj.kalium.crypto.Password
import org.abstractj.kalium.encoders.Encoder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.lifted.ProvenShape

case class User(email: String, timezone: ZoneId, approved: Boolean = false) extends HasId {
  override type IdType = Long
}

class Users(tag: Tag) extends IdTable[User](tag, "users") {
  def email = column[String]("email")

  def password = column[ByteString]("password")

  def timezone = column[ZoneId]("timezone")

  def approved = column[Boolean]("approved")

  def all = (email, timezone, approved) <> ((User.apply _).tupled, User.unapply)

  def withId = (id, all) <> ((WithId.apply[User] _).tupled, WithId.unapply[User])

  override def * : ProvenShape[WithId[User]] = withId
}

object Users {
  private[models] def tq = TableQuery[Users]

  private def hashPassword(password: String): ByteString = {
    val hasher = new Password

    ByteString(hasher.hash(password.getBytes("UTF-8"), Encoder.RAW,
      Sodium.CRYPTO_PWHASH_SCRYPTSALSA208SHA256_OPSLIMIT_INTERACTIVE,
      Sodium.CRYPTO_PWHASH_SCRYPTSALSA208SHA256_MEMLIMIT_INTERACTIVE
    ))
  }

  private def verifyPassword(hashed: ByteString, plaintext: String): Boolean = {
    val hasher = new Password

    hasher.verify(hashed.toArray, plaintext.getBytes("UTF-8"))
  }

  def findByUsernameWithPassword(email: String, password: String): DBIO[Option[WithId[User]]] =
    tq
      .filter(u => u.email === email && u.approved)
      .map(u => (u.withId, u.password))
      .result.headOption
      .map(_
        .filter(user => verifyPassword(user._2, password))
        .map(_._1)
      )

  def createUser(user: User, password: String): DBIO[WithId[User]] =
    (for {
      id <- tq.map(u => (u.all, u.password)).returning(tq.map(_.id)) += (user, hashPassword(password))
      user <- tq.filter(_.id === id).result.head
    } yield user).transactionally
}
