package services

import java.net.{InetSocketAddress, Socket}

import controllers.OurPlaySpec
import org.scalatest.concurrent.Eventually
import org.scalatestplus.play.guice.GuiceOneAppPerTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MessageUpdaterSpec extends OurPlaySpec with GuiceOneAppPerTest with Eventually {
  "MessageUpdater.isDeviceConnected" should {
    "return false when no device is connected" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      val connection = messageUpdater.isDeviceConnected
      connection mustBe false
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
      val msg = messageUpdater.setMessage(1,"hej")
    }
  }
}

