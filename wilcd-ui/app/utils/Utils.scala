package utils

import scala.concurrent.{ExecutionContext, Future}

object Utils {
  def futureSequenceOptional[T](f: Option[Future[T]])(implicit ec: ExecutionContext): Future[Option[T]] = f match {
    case Some(x) => x.map(Some(_))
    case None => Future.successful(None)
  }
}
