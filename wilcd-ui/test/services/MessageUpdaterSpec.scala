package services

import java.net.{InetSocketAddress, Socket}
import java.time.Instant

import controllers.OurPlaySpec
import models.{Id, Message, User}
import org.scalatest.concurrent.Eventually
import org.scalatestplus.play.guice.GuiceOneAppPerTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MessageUpdaterSpec extends OurPlaySpec with GuiceOneAppPerTest with Eventually {
  "MessageUpdater.isDeviceConnected" should {
    "return false when no device is connected" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      Await.result(messageUpdater.isDeviceConnected, Duration.Inf) mustBe false
    }

    "return true when device is connected" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      val socket = new Socket()
      socket.setSoTimeout(1000)
      socket.connect(new InetSocketAddress("127.0.0.1", 9797))
      eventually {
        Await.result(messageUpdater.isDeviceConnected, Duration.Inf) mustBe true
      }
    }
  }
  "MessageUpdater.setMessage" should {
    "update the message on the display" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      Await.result(messageUpdater.setMessage(Id[User](1),"hej"), Duration.Inf)
      Await.result(messageUpdater.getMessage, Duration.Inf).get.message mustBe "hej"
    }
  }
  "MessageUpdater.scheduleMessage" should {
    "schedule the entered message" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      val msg = Await.result(messageUpdater.scheduleMessage(Message(Id[User](1),"hej")), Duration.Inf).message
      Await.result(messageUpdater.getScheduledMessages,Duration.Inf).head.message mustBe "hej"
    }
  }
}

