package controllers

import org.scalatestplus.play.PlaySpec
import play.api.{Application, Play}

abstract class OurPlaySpec extends PlaySpec {
  def withApp[T](app: Application)(f: Application => T): T = {
    Play.start(app)
    try {
      f(app)
    } finally {
      Play.stop(app)
    }
  }
}
