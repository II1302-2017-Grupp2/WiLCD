package models

import PgProfile.api._
import slick.ast.TypedType

case class Id[T <: HasId](id: T#IdType) extends AnyVal

case class WithId[T <: HasId](id: Id[T], value: T)

trait HasId {
  type IdType
}

abstract class IdTable[T <: HasId](tag: Tag, name: String)(implicit idTT: TypedType[Id[T]]) extends Table[WithId[T]](tag, name) {
  def id = column[Id[T]]("id", O.PrimaryKey)
}
