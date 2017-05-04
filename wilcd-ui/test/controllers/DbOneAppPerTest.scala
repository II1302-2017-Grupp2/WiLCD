package controllers

import models.PgProfile
import models.PgProfile.api._
import org.scalatest.{Outcome, TestSuite}
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerTest}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.DatabaseConfig
import slick.dbio.DBIO

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

trait DbOneAppPerTest extends GuiceOneAppPerTest {
  this: TestSuite =>
  private def runDbQuery[T](sql: DBIO[T]): T = {
    val db = DatabaseConfig.forConfig[PgProfile]("slick.dbs.default").db
    val result = Await.result(
      db.run(sql),
      atMost = 2.seconds
    )
    db.close()
    result
  }

  override def fakeApplication(): Application = {
    val schema = s"test_${Math.abs(Random.self.nextLong())}"
    runDbQuery(sql"CREATE SCHEMA #$schema".asUpdate)
    val app = new GuiceApplicationBuilder()
      .configure(
        "slick.dbs.default.db.url" -> s"jdbc:postgresql://localhost:5432/wilcd?currentSchema=$schema",
        "testSchema" -> schema
      )
      .build()
    app
  }

  abstract override def withFixture(test: NoArgTest): Outcome = {
    val result = super.withFixture(test)
    runDbQuery(sql"DROP SCHEMA #${app.configuration.getString("testSchema").get} CASCADE".asUpdate)
    result
  }
}
