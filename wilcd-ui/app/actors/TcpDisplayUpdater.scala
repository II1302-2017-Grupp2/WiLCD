package actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.{IO, Tcp}
import Tcp._
import actors.TcpDisplayUpdater.{IsDeviceConnected, Update}
import akka.util.ByteString

import scala.collection.mutable

class TcpDisplayUpdater extends Actor with ActorLogging {
  import context.system

  private val io = IO(Tcp)
  private val clients = mutable.Set[ActorRef]()

  private var lastMessage = ByteString("")

  io ! Bind(self, new InetSocketAddress(9797))

  override def receive: Receive = {
    case Bound(addr) =>
      log.info(s"Listening on $addr")

    case Connected(remote, local) =>
      val conn = sender()
      log.info(s"Connection from $remote")
      clients += conn
      conn ! Register(self)
      conn ! Write(lastMessage)

    case Received(_) =>

    case PeerClosed =>
      log.info(s"Client disconnected: ${sender()}")
      clients -= sender()

    case Update(msg) =>
      log.info(s"Updating message to $msg")
      val bs = ByteString("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n"+msg, "ISO-8859-1")
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
}
