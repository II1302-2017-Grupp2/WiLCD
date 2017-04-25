package models

case class Id[T <: HasId](id: T#IdType) extends AnyVal

case class WithId[T <: HasId](id: Id[T], value: T)

trait HasId {
  type IdType
}
