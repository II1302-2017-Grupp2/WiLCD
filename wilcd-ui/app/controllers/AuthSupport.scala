package controllers

import java.util.UUID

import models.{Id, User, UserSession, WithId}
import play.api.mvc._
import services.UserService

import scala.async.Async
import Async._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.Utils

case class UserRequest[T](user: Option[WithId[User]], userSession: Option[WithId[UserSession]], request: Request[T]) extends WrappedRequest[T](request)

trait AuthSupport {
  private val SESSION = "session"
  protected def userService: UserService

  def setUserSession(res: Result, session: Id[UserSession]): Result = res.withSession(SESSION -> session.id.toString)
  def getUserSession(req: RequestHeader): Option[Id[UserSession]] = req.session.get(SESSION).map(UUID.fromString).map(Id[UserSession])

  object UserAction extends ActionBuilder[UserRequest] with ActionTransformer[Request, UserRequest] {
    override protected def transform[A](request: Request[A]): Future[UserRequest[A]] = Async.async {
      val sessionId = getUserSession(request)
      val session = await(Utils.futureSequenceOptional(sessionId.map(userService.findSession))).flatten
      UserRequest(session.map(_._1), session.map(_._2), request)
    }
  }

  object UserRequiredAction extends ActionFilter[UserRequest] {
    override protected def filter[A](request: UserRequest[A]): Future[Option[Result]] = Future.successful {
      request.user match {
        case None =>
          Some(Results.Redirect(routes.HomeController.signIn()))
        case Some(_) =>
          None
      }
    }
  }
}
