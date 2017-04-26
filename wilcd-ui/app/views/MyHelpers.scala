package views

import play.api.data.Field
import views.html.helper._

object MyHelpers {
  implicit val myFields = FieldConstructor(html.myFieldConstructor.f)
}
