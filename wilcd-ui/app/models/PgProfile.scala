package models

import com.github.tminglei.slickpg.{ExPostgresProfile, PgDate2Support, PgEnumSupport, PgNetSupport}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

class PgProfile extends ExPostgresProfile with PgDate2Support with PgNetSupport with PgEnumSupport {
  override val api = MyAPI

  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  object MyAPI extends API
    with DateTimeImplicits
    with NetImplicits {
    implicit val messageOccurrenceTypeMapper = createEnumJdbcType("message_occurrence", Message.Occurrence)
  }

}

object PgProfile extends PgProfile
