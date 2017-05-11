package actors

import java.net.InetSocketAddress
import java.time.{Instant, ZoneId}

import actors.TcpDisplayUpdater._
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.pattern.pipe
import akka.util.ByteString

import scala.collection.mutable
import scala.concurrent.Promise

class TcpDisplayUpdater extends Actor with ActorLogging {

  import context.{dispatcher, system}

  private val charset = "ISO-8859-1"
  private val timeZone = ZoneId.of("Europe/Stockholm")

  private val io = IO(Tcp)
  private val bindPromise = Promise[NowBound.type]()
  private val clients = mutable.Set[ActorRef]()

  private var lastMessage = ByteString("")

  io ! Bind(self, new InetSocketAddress(9797))

  override def receive: Receive = {
    case Bound(addr) =>
      log.info(s"Listening on $addr")
      bindPromise.success(NowBound)

    case NotifyBound =>
      bindPromise.future pipeTo sender()

    case Connected(remote, local) =>
      val conn = sender()
      log.info(s"Connection from $remote")
      clients += conn
      conn ! Register(self)
      conn ! Write(ByteString("T" + Instant.now().atZone(timeZone).toLocalDateTime + "\r\n"))
      conn ! Write(lastMessage)

    case Received(_) =>

    case PeerClosed =>
      log.info(s"Client disconnected: ${sender()}")
      clients -= sender()

    case Update(msg) =>
      log.info(s"Updating message to $msg")
      val bs = ByteString("M" + msg + "\r\n", charset)
      for (client <- clients) {
        client ! Write(bs)
      }
      lastMessage = bs

    case IsDeviceConnected =>
      sender() ! clients.nonEmpty
  }
}

object TcpDisplayUpdater {

  case class Update(msg: String)

  case object IsDeviceConnected

  case object NotifyBound

  case object NowBound

}
