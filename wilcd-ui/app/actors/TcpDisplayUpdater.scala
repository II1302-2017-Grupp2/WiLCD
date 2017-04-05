package actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.{IO, Tcp}
import Tcp._
import actors.TcpDisplayUpdater.Update
import akka.util.ByteString

import scala.collection.mutable

class TcpDisplayUpdater extends Actor with ActorLogging {
  import context.system

  private val io = IO(Tcp)
  private val clients = mutable.Set[ActorRef]()

  io ! Bind(self, new InetSocketAddress(9797))

  override def receive: Receive = {
    case Bound(addr) =>
      log.info(s"Listening on $addr")

    case Connected(remote, local) =>
      val conn = sender()
      log.info(s"Connection from $remote")
      clients += conn
      conn ! Register(self)

    case Received(_) =>

    case PeerClosed =>
      println(clients)
      clients -= sender()

    case Update(msg) =>
      val bs = ByteString(msg+"\r\n")
      for (client <- clients) {
        client ! Write(bs)
      }
  }
}

object TcpDisplayUpdater {
  case class Update(msg: String)
}
