package models

case class User(username: String) extends HasId {
  override type IdType = Long
}
