package utils

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import karedo.util.{Configurable, KO, OK}
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification

trait MongoTestUtils extends Configurable {
  self: Specification  =>
  def beOK[T]: AnyRef with Matcher[AnyRef] = {
    beAnInstanceOf[OK[String, T]]
  }
  def beKO[T]: AnyRef with Matcher[AnyRef] = {
    beAnInstanceOf[KO[String, T]]
  }
  DbMongoDAO.tablePrefix = "TestPersist_"
  implicit def stringWrapper(u: UUID) = u.toString
}
