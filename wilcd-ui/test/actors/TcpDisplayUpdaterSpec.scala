package actors

import java.net.{InetSocketAddress, Socket}
import java.util.Scanner

import actors.TcpDisplayUpdater.{IsDeviceConnected, Update}
import akka.actor.ActorRef
import akka.pattern.ask
import controllers.{DbOneAppPerTest, OurPlaySpec}
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.inject.BindingKey

import scala.concurrent.Await
import scala.concurrent.duration._

class TcpDisplayUpdaterSpec extends OurPlaySpec with DbOneAppPerTest with Eventually {
  "TcpDisplayUpdater ! Update" should {
    "Send the updated message to all clients" in {
      implicit val timeout = akka.util.Timeout(1.second)
      val updater = app.injector.instanceOf(BindingKey(classOf[ActorRef]).qualifiedWith("tcp-display-updater"))
      val socket = new Socket()
      socket.setSoTimeout(1000)
      socket.connect(new InetSocketAddress("127.0.0.1", 9797))
      eventually(Timeout(Span(4, Seconds))) {
        Await.result(updater ? IsDeviceConnected, 1.second) mustBe true
      }
      updater ! Update("blah")

      val inputStream = socket.getInputStream
      val scanner = new Scanner(inputStream)
      eventually {
        val line = scanner.nextLine()
        line mustBe "blah"
      }
    }
  }
}
