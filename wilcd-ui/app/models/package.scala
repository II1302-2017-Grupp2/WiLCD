import java.net.InetAddress

import akka.util.ByteString
import com.github.tminglei.slickpg.InetString
import models.PgProfile.api._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import scala.language.implicitConversions

package object models {
  implicit def idTypedType[T <: HasId](implicit baseTT: BaseColumnType[T#IdType]): JdbcType[Id[T]] with BaseTypedType[Id[T]] = MappedColumnType.base[Id[T], T#IdType](
    _.id,
    Id[T]
  )

  implicit def byteStringTypedType: JdbcType[ByteString] with BaseTypedType[ByteString] = MappedColumnType.base[ByteString, Array[Byte]](
    _.toArray[Byte],
    ByteString(_)
  )

  implicit def inetAddressTypedType: JdbcType[InetAddress] with BaseTypedType[InetAddress] = MappedColumnType.base[InetAddress, InetString](
    addr => InetString(addr.getHostAddress),
    addr => InetAddress.getByName(addr.value)
  )

  implicit def withId2id[T <: HasId](value: WithId[T]): Id[T] = value.id
  implicit def withId2value[T <: HasId](value: WithId[T]): T = value.value
}
