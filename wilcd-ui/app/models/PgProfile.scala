package models

import com.github.tminglei.slickpg.{ExPostgresProfile, PgDate2Support}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

class PgProfile extends ExPostgresProfile with PgDate2Support {
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = MyAPI

  object MyAPI extends API
    with DateTimeImplicits
}

object PgProfile extends PgProfile
