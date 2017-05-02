package utils

import java.time.{DateTimeException, ZoneId}

import play.api.data.FormError
import play.api.data.format.Formatter

object ExtraFormatters {
  implicit def zoneIdFormatter(implicit textFormatter: Formatter[String]): Formatter[ZoneId] = new Formatter[ZoneId] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ZoneId] =
      textFormatter.bind(key, data).right.flatMap {
        case "UTC (default)" =>
          Right(ZoneId.of("UTC"))
        case name => try {
          Right(ZoneId.of(name))
        } catch {
          case _: DateTimeException =>
            Left(Seq(FormError(key, "error.zoneId")))
        }
      }

    override def unbind(key: String, value: ZoneId): Map[String, String] = textFormatter.unbind(key, value.toString)
  }

  def enumFormatter(enum: Enumeration)(implicit textFormatter: Formatter[String]): Formatter[enum.Value] = new Formatter[enum.Value] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], enum.Value] =
    textFormatter.bind(key, data).right.flatMap { name =>
      try {
        Right(enum.withName(name))
      } catch {
        case _: NoSuchElementException =>
          Left(Seq(FormError(key, s"error.enum.${enum.toString()}")))
      }
    }

    override def unbind(key: String, value: enum.Value): Map[String, String] = textFormatter.unbind(key, value.toString)
  }
}
