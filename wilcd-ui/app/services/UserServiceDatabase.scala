package services

import java.net.InetAddress
import javax.inject.Inject

import models.PgProfile.api._
import models.{Id, PgProfile, User, UserSession, UserSessions, Users, WithId}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class UserServiceDatabase @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends UserService with HasDatabaseConfigProvider[PgProfile] {
  override def create(user: User, password: String): Future[Option[WithId[User]]] = db.run(Users.createUser(user, password).asTry.map(_.toOption))

  override def logIn(email: String, password: String, ip: InetAddress): Future[Option[WithId[UserSession]]] = db.run(
    Users.findByUsernameWithPassword(email, password).flatMap(userM =>
      DBIO.sequenceOption(
        userM.map(user =>
          UserSessions.create(user, ip)
        )
      )
    )
  )

  override def logOut(id: Id[UserSession]): Future[Unit] = db.run(UserSessions.destroy(id))

  override def changePassword(id: Id[User], oldPassword: String, newPassword: String): Future[Boolean] = db.run(Users.changePassword(id, oldPassword, newPassword))

  override def changeSettings(id: Id[User], f: (User) => User): Future[Unit] = db.run(Users.modifyUser(id, f))

  override def findSession(id: Id[UserSession]): Future[Option[(WithId[User], WithId[UserSession])]] = db.run(UserSessions.find(id).result.headOption)
}
