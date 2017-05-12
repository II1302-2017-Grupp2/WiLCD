package controllers

import java.net.InetAddress
import java.util.UUID

import models.{Id, User, UserSession, WithId}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.UserService
import utils.Utils

import scala.async.Async
import scala.async.Async._
import scala.concurrent.Future

case class UserRequest[T](user: Option[WithId[User]], userSession: Option[WithId[UserSession]], request: Request[T]) extends WrappedRequest[T](request)

trait AuthSupport {
  private val SESSION = "session"

  def setUserSession(res: Result, session: Id[UserSession]): Result = res.withSession(SESSION -> session.id.toString)

  def getUserSession(req: RequestHeader): Option[Id[UserSession]] = req.session.get(SESSION).map(UUID.fromString).map(Id[UserSession])

  protected def userService: UserService

  object UserAction extends ActionBuilder[UserRequest] with ActionTransformer[Request, UserRequest] {
    override protected def transform[A](request: Request[A]): Future[UserRequest[A]] = Async.async {
      val sessionId = getUserSession(request)
      val session = await(Utils.futureSequenceOptional(sessionId.map(userService.findSession)))
        .flatten
        .filter(_._2.ip == InetAddress.getByName(request.remoteAddress))
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
