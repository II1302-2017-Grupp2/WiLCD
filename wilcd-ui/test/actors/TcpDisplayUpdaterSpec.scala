package actors

import java.net.{InetSocketAddress, Socket}
import java.util.Scanner
import javax.inject.Qualifier

import akka.actor.ActorRef
import controllers.OurPlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.{BindingKey, QualifierAnnotation}
import services.MessageUpdaterNetwork

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.scalatest.concurrent.Eventually

class TcpDisplayUpdaterSpec extends OurPlaySpec with GuiceOneAppPerTest with Eventually {
  "TcpDisplayUpdater ! Update" should {
    "Send the updated message to all clients" in {
      val updater = app.injector.instanceOf[MessageUpdaterNetwork]
      val socket = new Socket()
      socket.setSoTimeout(1000)
      socket.connect(new InetSocketAddress("127.0.0.1", 9797))
      eventually {
        Await.result(updater.isDeviceConnected, Duration.Inf) mustBe true
      }
      updater.setMessage("blah")
      val inputStream = socket.getInputStream
      val scanner = new Scanner(inputStream)
      scanner.nextLine() mustBe "blah"
    }
  }
}
