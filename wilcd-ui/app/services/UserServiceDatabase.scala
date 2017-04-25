package services
import javax.inject.Inject

import models.{PgProfile, User, WithId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.Future

class UserServiceDatabase @Inject() (val dbConfigProvider: DatabaseConfigProvider) extends UserService with HasDatabaseConfigProvider[PgProfile] {
  override def create(username: String, password: String): Future[WithId[User]] = db.run(User.createUser(username, password))

  override def logIn(username: String, password: String): Future[Option[WithId[User]]] = db.run(User.findByUsernameWithPassword(username, password))
}
