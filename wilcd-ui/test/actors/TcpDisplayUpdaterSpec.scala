package actors

import java.net.{InetSocketAddress, Socket}
import java.time.ZoneId
import java.util.Scanner

import controllers.{DbOneAppPerTest, OurPlaySpec}
import models.User
import org.scalatest.concurrent.Eventually
import services.{MessageUpdater, UserService}

import scala.concurrent.Await
import scala.concurrent.duration._

class TcpDisplayUpdaterSpec extends OurPlaySpec with DbOneAppPerTest with Eventually {
  "TcpDisplayUpdater ! Update" should {
    "Send the updated message to all clients" in {
      val updater = app.injector.instanceOf[MessageUpdater]
      val users = app.injector.instanceOf[UserService]
      val user = Await.result(users.create(User("a@a", ZoneId.of("UTC")), ""), 1.second).get
      val socket = new Socket()
      socket.setSoTimeout(1000)
      socket.connect(new InetSocketAddress("127.0.0.1", 9797))
      eventually {
        Await.result(updater.isDeviceConnected, Duration.Inf) mustBe true
      }
      Await.result(updater.setMessage(user, "blah"), 1.second)
      val inputStream = socket.getInputStream
      val scanner = new Scanner(inputStream)
      eventually {
        val line = scanner.nextLine()
        line mustBe "blah"
      }
    }
  }
}
