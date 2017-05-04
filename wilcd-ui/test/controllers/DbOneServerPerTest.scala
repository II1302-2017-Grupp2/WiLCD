package controllers

import org.scalatest.TestSuite
import org.scalatestplus.play.BaseOneServerPerTest

trait DbOneServerPerTest extends BaseOneServerPerTest with DbFakeApplicationFactory {
  this: TestSuite =>
}
