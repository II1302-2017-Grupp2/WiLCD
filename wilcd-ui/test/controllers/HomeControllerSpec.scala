package controllers

import models.{Id, User}
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.inject._
import play.api.inject.guice._
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Play}
import services.MessageUpdater

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/*/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest {


  "HomeController GET" should {

    /*"render the index page from a new instance of controller" in {
      val controller = new HomeController
      val home = controller.index().apply(FakeRequest())

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Welcome to Play")
    }*/

    "render the index page from the application" in {
      val controller = app.injector.instanceOf[HomeController]
      val home = controller.index().apply(FakeRequest())

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

    "render the index page from the router" in {
      // Need to specify Host header to get through AllowedHostsFilter
      val request = FakeRequest(GET, "/").withHeaders("Host" -> "localhost")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }
  }

}*/

class HomeControllerSpec extends OurPlaySpec with MockitoSugar {
  "HomeController.submitNewMessage" should {
    "invoke the MessageUpdater" in {
      val messageUpdaterMock = mock[MessageUpdater]
      val userId = Id[User](-1)
      Mockito.when(messageUpdaterMock.setMessage(userId, "HELLO, WORLD!")).thenReturn(Future.successful(()))
      withApp(new GuiceApplicationBuilder()
        .overrides(bind[MessageUpdater].toInstance(messageUpdaterMock))
        .build()) { app =>

        val call = routes.HomeController.submitNewMessage()
        val request = FakeRequest(call.method, s"${call.url}?message=HELLO,%20WORLD!")
        val result = Await.result(route(app, request).get, Duration.Inf)

        Mockito.verify(messageUpdaterMock).setMessage(userId, "HELLO, WORLD!")
        result.header.status mustBe OK
      }
    }
  }

}
