package controllers

import java.net.InetAddress
import java.time.{Instant, LocalDateTime, ZoneId}
import javax.inject._

import controllers.HomeController.{SigninData, SignupData, UpdateMessageData}
import models.{Id, Message, User}
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.twirl.api.Html
import services.{MessageUpdater, UserService}
import utils.ExtraFormatters._

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
      "password" -> nonEmptyText,
      "timezone" -> of[ZoneId]
    )(SignupData.apply)(SignupData.unapply)
  )
  val updateMessageForm = Form(
    mapping(
      "message" -> text,
      "displayFrom" -> optional(localDateTime("yyyy-MM-dd HH:mm")),
      "displayUntil" -> optional(localDateTime("yyyy-MM-dd HH:mm")),
      "occurrence" -> default(of[Message.Occurrence](enumFormatter(Message.Occurrence)), Message.Occurrence.Once)
    )(UpdateMessageData.apply)(UpdateMessageData.unapply)
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
        case Some(session) =>
          setUserSession(Redirect(routes.HomeController.index()), session)
            .flashing("message" -> "You have been signed in")
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
        user <- userService.create(User(formData.email, formData.timezone), formData.password)
        Some(session) <- userService.logIn(formData.email, formData.password, InetAddress.getByName(request.remoteAddress))
      } yield setUserSession(Redirect(routes.HomeController.index()), session)
        .flashing("message" -> "Your account has been created")
    )
  }

  def signOut = UserAction.async { implicit request =>
    for {
      _ <- userService.logOut(request.userSession.get)
    } yield Redirect(routes.HomeController.index())
      .flashing("message" -> "You have been signed out")
  }

  def instantMessage = UserAction { implicit request =>
    Ok(views.html.instantMessage())
  }

  private def scheduleMessagePage(form: Form[UpdateMessageData])(implicit request: UserRequest[_]): Future[Html] =
    for {
      (deviceConnected, scheduledMessages) <- messageUpdater.isDeviceConnected.zip(messageUpdater.getScheduledMessages)
    } yield views.html.scheduleMessage(form, scheduledMessages, deviceConnected = deviceConnected)

  def scheduleMessage = UserAction.async { implicit request =>
    scheduleMessagePage(updateMessageForm).map(Ok(_))
  }

  def submitNewMessage = UserAction.async { implicit request =>
    val user = request.user.get
    updateMessageForm.bindFromRequest().fold(
      formWithErrors => scheduleMessagePage(formWithErrors).map(BadRequest(_)),
      formData => for {
        message <- messageUpdater.scheduleMessage(Message(
          user,
          formData.message,
          displayFrom = formData.displayFrom
            .map(_.atZone(user.timezone).toInstant)
            .getOrElse(Instant.now()),
          displayUntil = formData.displayUntil
            .map(_.atZone(user.timezone).toInstant),
          occurrence = formData.occurrence
        ))
      } yield Redirect(routes.HomeController.scheduleMessage().withFragment(s"message-${message.id.id}"))
        .flashing("message" -> "Your message has been scheduled")
    )
  }

  def deleteMessage(id: Id[Message]) = TODO

  def doDeleteMessage(id: Id[Message]) = TODO
}

object HomeController {

  case class SigninData(email: String, password: String)

  case class SignupData(email: String, password: String, timezone: ZoneId)

  case class UpdateMessageData(message: String, displayFrom: Option[LocalDateTime], displayUntil: Option[LocalDateTime], occurrence: Message.Occurrence)

}
