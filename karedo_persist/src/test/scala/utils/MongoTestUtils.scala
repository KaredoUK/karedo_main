package utils

import java.util.UUID

import karedo.common.result.{Result, OK, KO}
import karedo.persist.entity.dao._
import karedo.route.util.Configurable
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification

trait MongoTestUtils extends Configurable {
  self: Specification  =>
  def beOK[T]: AnyRef with Matcher[AnyRef] = {
    beAnInstanceOf[OK[T]]
  }
  def beKO[T]: AnyRef with Matcher[AnyRef] = {
    beAnInstanceOf[KO[String]]
  }
  DbDAOParams.tablePrefix = "TestPersist_"
  implicit def stringWrapper(u: UUID) = u.toString
}
