package utils

import java.time.Duration
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object Utils {
  def futureSequenceOptional[T](f: Option[Future[T]])(implicit ec: ExecutionContext): Future[Option[T]] = f match {
    case Some(x) => x.map(Some(_))
    case None => Future.successful(None)
  }

  def optionMin[T: Ordering](opts: Option[T]*): Option[T] = opts.flatten.sorted.headOption

  implicit def jduration2finiteDuration(duration: Duration): FiniteDuration = FiniteDuration(duration.toNanos, TimeUnit.NANOSECONDS)
}
