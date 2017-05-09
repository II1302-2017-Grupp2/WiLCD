package services

import java.net.{InetSocketAddress, Socket}
import java.util.Scanner

import controllers.{DbOneAppPerTest, OurPlaySpec}
import models.{Id, Message}
import org.scalatest.concurrent.Eventually

import scala.concurrent.Await
import scala.concurrent.duration._

class MessageUpdaterSpec extends OurPlaySpec with DbOneAppPerTest with Eventually {
  "MessageUpdater.isDeviceConnected" should {
    "return false when no device is connected" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      Await.result(messageUpdater.isDeviceConnected, Duration.Inf) mustBe false
    }

    "return true when device is connected" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      Await.result(messageUpdater.ready, 1.second)
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
      Await.result(messageUpdater.setMessage(user, "hej"), Duration.Inf)
      Await.result(messageUpdater.getMessage, Duration.Inf).get.message mustBe "hej"
    }
  }
  "MessageUpdater.scheduleMessage" should {
    "schedule the entered message" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      Await.result(messageUpdater.ready, 1.second)
      val msg = Await.result(messageUpdater.scheduleMessage(Message(user, "hej")), Duration.Inf).message
      val socket = new Socket()
      socket.setSoTimeout(1000)
      socket.connect(new InetSocketAddress("127.0.0.1", 9797))
      Await.result(messageUpdater.getScheduledMessages, Duration.Inf).head.message mustBe "hej"
      val inputStream = socket.getInputStream
      val scanner = new Scanner(inputStream)
      eventually {
        val line = scanner.nextLine()
        line mustBe "hej"
      }
    }
  }
  "MessageUpdater.deleteMessage" should {
    "delete the entered message" in {
      val messageUpdater = app.injector.instanceOf[MessageUpdater]
      Await.result(messageUpdater.setMessage(user,"hej"), Duration.Inf)
      Await.result(messageUpdater.deleteMessage(user,Id[Message](1)), Duration.Inf)
    }
  }
  override protected val createUser = true
}

