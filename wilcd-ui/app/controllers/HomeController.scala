package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import services.MessageUpdater
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.twirl.api.Html

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (messageUpdater: MessageUpdater) extends Controller {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    messageUpdater.setMessage("hello")
    Ok(views.html.index())
  }

  def submitNewMessage = Action.async { request =>
    for {
      () <- messageUpdater.setMessage(request.getQueryString("message").get)
    } yield Ok(Html("<body>DONE</body>"))
  }
}
