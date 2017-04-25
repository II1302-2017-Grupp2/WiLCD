import models.PgProfile.api._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

package object models {
  implicit def idTypedType[T <: HasId](implicit baseTT: BaseColumnType[T#IdType]): JdbcType[Id[T]] with BaseTypedType[Id[T]] = MappedColumnType.base[Id[T], T#IdType](
    _.id,
    Id[T]
  )
}
