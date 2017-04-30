package services

import actors.{DbMessageFetcher, TcpDisplayUpdater}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class MyModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    //    bind(classOf[MessageUpdater]).to(classOf[MessageUpdaterNoop])
    bind(classOf[MessageUpdater]).to(classOf[MessageUpdaterDatabase])
    bind(classOf[UserService]).to(classOf[UserServiceDatabase])
    bindActor[TcpDisplayUpdater]("tcp-display-updater")
    bindActor[DbMessageFetcher]("db-message-fetcher")
  }
}
