package controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.scalatestplus.play.{ChromeFactory, OneBrowserPerSuite}
import services.MessageUpdater

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class HomeControllerIntegrationSpec extends OurPlaySpec with GuiceOneServerPerTest with MockitoSugar with OneBrowserPerSuite with ChromeFactory {
  "Setting a new message" must {
    "update the current message" in {
      go to s"http://localhost:$port/"
      textField(name("message")).value = "Hello, World"
      submit()
      eventually {
        find(tagName("body")).get.text must include("Your message has been scheduled")
      }

      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      val message = Await.result(messageUpdater.getMessage, Duration.Inf)
      message mustBe "Hello, World"
    }
  }
}
