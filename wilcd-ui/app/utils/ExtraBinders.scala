package utils

import models.{HasId, Id}
import play.api.mvc.PathBindable

object ExtraBinders {
  implicit def idPathBindable[T <: HasId](implicit inner: PathBindable[T#IdType]): PathBindable[Id[T]] =
    inner.transform(
      Id[T],
      _.id
    )
}
