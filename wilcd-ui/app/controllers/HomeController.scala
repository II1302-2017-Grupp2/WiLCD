package controllers

import java.net.InetAddress
import java.time.{Instant, LocalDateTime, ZoneId}
import javax.inject._

import controllers.HomeController.{SettingsData, SigninData, SignupData, UpdateMessageData}
import models.{Id, Message, User}
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.twirl.api.Html
import services.MessageUpdater.DeleteResult
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
      "confirmPassword" -> text,
      "timezone" -> of[ZoneId]
    )(SignupData.apply)(SignupData.unapply)
      .verifying("error.password.confirm", data => data.password == data.confirmPassword)
  )
  val settingsForm = Form(
    mapping(
      "oldPassword" -> text,
      "newPassword" -> text,
      "confirmPassword" -> text,
      "timezone" -> of[ZoneId]
    )(SettingsData.apply)(SettingsData.unapply)
      .verifying("error.password.oldnew", data => if (data.newPassword.nonEmpty) data.oldPassword.nonEmpty else true)
      .verifying("error.password.confirm", data => data.newPassword == data.confirmPassword)
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
  def index = UserAction.async { implicit request =>
    for {
      message <- messageUpdater.getMessage
    } yield
      request.user match {
        case None =>
          Ok(views.html.index(message))
        case Some(_) =>
          Ok(views.html.instantMessage(message))
      }
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
        case None => BadRequest(views.html.signIn(form.withGlobalError("Wrong email and/or password")))
      }
    )
  }

  def signUp = UserAction { implicit request =>
    Ok(views.html.signUp(signupForm))
  }

  def doSignUp = UserAction.async { implicit request =>
    val form = signupForm.bindFromRequest()
    form.fold(
      formWithErrors => Future.successful(BadRequest(views.html.signUp(formWithErrors))),
      formData =>
        userService.create(User(formData.email, formData.timezone), formData.password).map {
          case None =>
            BadRequest(views.html.signUp(form.withError("email", "That email address is already in use")))
          case Some(user) =>
            Redirect(routes.HomeController.index())
              .flashing("message" -> "Your account has been created, and will be usable as soon as it is approved")
        }
    )
  }

  def settings = UserAction.andThen(UserRequiredAction) { implicit request =>
    Ok(views.html.settings(settingsForm.fill(SettingsData(
      oldPassword = "", newPassword = "", confirmPassword = "",
      timezone = request.user.get.timezone
    ))))
  }

  def saveSettings = UserAction.andThen(UserRequiredAction).async { implicit request =>
    val form = settingsForm.bindFromRequest()
    form.fold(
      formWithErrors => Future.successful(BadRequest(views.html.settings(formWithErrors))),
      formData => for {
        pwChangeSuccess <- if (formData.newPassword.nonEmpty)
          userService.changePassword(request.user.get, formData.oldPassword, formData.newPassword)
        else
          Future.successful(true)
        _ <- userService.changeSettings(request.user.get, _.copy(timezone = formData.timezone))
      } yield if (pwChangeSuccess)
        Redirect(routes.HomeController.settings())
          .flashing("message" -> "Your settings have been saved")
      else
        BadRequest(views.html.settings(form.withError("oldPassword", "error.password")))
    )
  }

  def signOut = UserAction.andThen(UserRequiredAction).async { implicit request =>
    for {
      _ <- userService.logOut(request.userSession.get)
    } yield Redirect(routes.HomeController.index())
      .flashing("message" -> "You have been signed out")
  }

  def scheduleMessage = UserAction.andThen(UserRequiredAction).async { implicit request =>
    scheduleMessagePage(updateMessageForm).map(Ok(_))
  }

  private def submitNewMessage(target: Call) = UserAction.andThen(UserRequiredAction).async { implicit request =>
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
      } yield Redirect(target
        .withFragment(s"message-${message.id.id}"))
        .flashing("message" -> "Your message has been scheduled")
    )
  }

  def doScheduleMessage = submitNewMessage(routes.HomeController.scheduleMessage())
  def doShowInstantMessage = submitNewMessage(routes.HomeController.index())

  private def scheduleMessagePage(form: Form[UpdateMessageData])(implicit request: UserRequest[_]): Future[Html] =
    for {
      (deviceConnected, scheduledMessages) <- messageUpdater.isDeviceConnected.zip(messageUpdater.getScheduledMessages)
    } yield views.html.scheduleMessage(form, scheduledMessages, deviceConnected = deviceConnected)

  def deleteMessage(id: Id[Message]) = UserAction.andThen(UserRequiredAction) { implicit request =>
    Ok(views.html.deleteMessage(id))
  }

  def doDeleteMessage(id: Id[Message]) = UserAction.andThen(UserRequiredAction).async { implicit request =>
    messageUpdater.deleteMessage(request.user.get, id).map {
      case DeleteResult.Success =>
        Seq("message" -> "The message has been deleted")
      case DeleteResult.NoPermission =>
        Seq(
          "message" -> "You are not authorized to delete that",
          "message.status" -> "danger"
        )
    }.map(Redirect(routes.HomeController.scheduleMessage()).flashing(_: _*))
  }
}

object HomeController {

  case class SigninData(email: String, password: String)

  case class SignupData(email: String, password: String, confirmPassword: String, timezone: ZoneId)

  case class SettingsData(oldPassword: String, newPassword: String, confirmPassword: String, timezone: ZoneId)

  case class UpdateMessageData(message: String, displayFrom: Option[LocalDateTime], displayUntil: Option[LocalDateTime], occurrence: Message.Occurrence)

}
