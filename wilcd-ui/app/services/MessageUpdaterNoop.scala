package services
import javax.inject.Inject

import play.api.Logger

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class MessageUpdaterNoop @Inject() extends MessageUpdater {
  private lazy val logger = Logger(getClass)

  override def setMessage(msg: String): Future[Unit] = {
    Future {
      logger.warn(s"Updated message: $msg")
    }
  }
}
