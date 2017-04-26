package models

import com.github.tminglei.slickpg.{ExPostgresProfile, PgDate2Support, PgNetSupport}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

class PgProfile extends ExPostgresProfile with PgDate2Support with PgNetSupport {
  override val api = MyAPI

  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  object MyAPI extends API
    with DateTimeImplicits
    with NetImplicits

}

object PgProfile extends PgProfile
