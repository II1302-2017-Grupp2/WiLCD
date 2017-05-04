package controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.scalatestplus.play.{ChromeFactory, OneBrowserPerSuite}
import services.MessageUpdater

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class HomeControllerIntegrationSpec extends OurPlaySpec with DbOneServerPerTest with MockitoSugar with OneBrowserPerSuite with ChromeFactory {
  "The basic user flow" must {
    "work" in {
      go to s"http://localhost:$port/"

      click on linkText("Sign up")
      emailField(name("email")).value = "a@a"
      pwdField(name("password")).value = "asdf"
      submit()
      find(tagName("body")).get.text must include("Your account has been created")

      click on linkText("Sign out")
      find(tagName("body")).get.text must include("You have been signed out")

      click on linkText("Sign in")
      emailField(name("email")).value = "a@a"
      pwdField(name("password")).value = "asdf"
      submit()
      find(tagName("body")).get.text must include("You have been signed in")

      click on linkText("Schedule message")
      textField(name("message")).value = "Hello, World"
      submit()
      find(tagName("body")).get.text must include("Your message has been scheduled")
      find(tagName("body")).get.text must include("Hello, World")

      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      val message = Await.result(messageUpdater.getMessage, Duration.Inf)
      message.get.message mustBe "Hello, World"
    }
  }
}
