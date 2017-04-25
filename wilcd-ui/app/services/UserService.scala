package services

import models.{User, WithId}

import scala.concurrent.Future

trait UserService {
  def create(username: String, password: String): Future[WithId[User]]
  def logIn(username: String, password: String): Future[Option[WithId[User]]]
}
