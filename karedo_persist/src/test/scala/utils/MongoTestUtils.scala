package utils

import com.typesafe.config.{Config, ConfigFactory}
import karedo.entity.dao.{Configurable, KO, OK}
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
}
