package controllers

import java.net.InetAddress
import javax.inject._

import controllers.HomeController.{SigninData, SignupData}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.twirl.api.Html
import services.{MessageUpdater, UserService}

import scala.concurrent.Future

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(messageUpdater: MessageUpdater, val userService: UserService, val messagesApi: MessagesApi) extends Controller with I18nSupport with AuthSupport {

  val signinForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(SigninData.apply)(SigninData.unapply)
  )
  val signupForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(SignupData.apply)(SignupData.unapply)
  )

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = UserAction { implicit request =>
    Ok(views.html.index())
  }

  def signIn = UserAction { implicit request =>
    Ok(views.html.signIn(signinForm))
  }

  def doSignIn = UserAction.async { implicit request =>
    val form = signinForm.bindFromRequest()
    form.fold(
      formWithErrors => Future.successful(BadRequest(views.html.signIn(formWithErrors))),
      formData => for {
        maybeSession <- userService.logIn(formData.email, formData.password, InetAddress.getByName(request.remoteAddress))
      } yield maybeSession match {
        case Some(session) => setUserSession(Redirect(routes.HomeController.index()), session)
        case None => BadRequest(views.html.signIn(form.withError("password", "Wrong email and/or password")))
      }
    )
  }

  def signUp = UserAction { implicit request =>
    Ok(views.html.signUp(signupForm))
  }

  def doSignUp = UserAction.async { implicit request =>
    signupForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.signUp(formWithErrors))),
      formData => for {
        user <- userService.create(formData.email, formData.password)
        Some(session) <- userService.logIn(formData.email, formData.password, InetAddress.getByName(request.remoteAddress))
      } yield setUserSession(Redirect(routes.HomeController.index()), session)
    )
  }

  def signOut = UserAction.async { implicit request =>
    userService.logOut(request.userSession.get).map(_ => Redirect(routes.HomeController.index()))
  }

  def instantMessage = UserAction { implicit request =>
    Ok(views.html.instantMessage())
  }

  def scheduleMessage = UserAction { implicit request =>
    Ok(views.html.scheduleMessage())
  }

  def submitNewMessage = UserAction.async { request =>
    for {
      () <- messageUpdater.setMessage(request.user.get, request.getQueryString("message").get)
    } yield Ok(Html("<body>DONE</body>"))
  }
}

object HomeController {

  case class SigninData(email: String, password: String)

  case class SignupData(email: String, password: String)

}
