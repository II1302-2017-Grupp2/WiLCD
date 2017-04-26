package models

import akka.util.ByteString
import models.PgProfile.api._
import org.abstractj.kalium.NaCl.Sodium
import org.abstractj.kalium.crypto.Password
import org.abstractj.kalium.encoders.Encoder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.lifted.ProvenShape

case class User(email: String) extends HasId {
  override type IdType = Long
}

class Users(tag: Tag) extends IdTable[User](tag, "users") {
  def email = column[String]("email")

  def password = column[ByteString]("password")

  def all = email <> (User.apply, User.unapply)

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
      .filter(_.email === email)
      .map(u => (u.withId, u.password))
      .result.headOption
      .map(_
        .filter(user => verifyPassword(user._2, password))
        .map(_._1)
      )

  def createUser(email: String, password: String): DBIO[WithId[User]] =
    (for {
      id <- tq.map(u => (u.all, u.password)).returning(tq.map(_.id)) += (User(email), hashPassword(password))
      user <- tq.filter(_.id === id).result.head
    } yield user).transactionally
}
