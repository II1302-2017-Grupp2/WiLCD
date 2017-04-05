package services

import actors.TcpDisplayUpdater
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class MyModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    //    bind(classOf[MessageUpdater]).to(classOf[MessageUpdaterNoop])
    bind(classOf[MessageUpdater]).to(classOf[MessageUpdaterNetwork])
    bindActor[TcpDisplayUpdater]("tcp-display-updater")
  }
}
