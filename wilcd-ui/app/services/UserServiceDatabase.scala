package services

import java.net.InetAddress
import javax.inject.Inject

import models.PgProfile.api._
import models.{PgProfile, User, UserSession, UserSessions, Users, WithId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class UserServiceDatabase @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends UserService with HasDatabaseConfigProvider[PgProfile] {
  override def create(email: String, password: String): Future[WithId[User]] = db.run(Users.createUser(email, password))

  override def logIn(email: String, password: String, ip: InetAddress): Future[Option[(WithId[User], WithId[UserSession])]] = db.run(
    Users.findByUsernameWithPassword(email, password).flatMap(userM =>
      DBIO.sequenceOption(
        userM.map(user =>
          UserSessions.create(user, ip).map(session => (user, session))
        )
      )
    )
  )
}
