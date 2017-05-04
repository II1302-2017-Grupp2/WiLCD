package controllers

import org.scalatest.TestSuite
import org.scalatestplus.play.BaseOneAppPerTest

trait DbOneAppPerTest extends BaseOneAppPerTest with DbFakeApplicationFactory {
  this: TestSuite =>
}
