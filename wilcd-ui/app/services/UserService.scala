package services

import java.net.InetAddress

import models.{Id, User, UserSession, WithId}

import scala.concurrent.Future

trait UserService {
  def create(email: String, password: String): Future[WithId[User]]

  def logIn(email: String, password: String, ip: InetAddress): Future[Option[WithId[UserSession]]]

  def logOut(id: Id[UserSession]): Future[Unit]

  def findSession(id: Id[UserSession]): Future[Option[(WithId[User], WithId[UserSession])]]
}
