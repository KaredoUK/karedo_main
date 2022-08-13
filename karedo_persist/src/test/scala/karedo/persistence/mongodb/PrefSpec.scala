package karedo.persistence.mongodb

import java.util.UUID

import karedo.entity._
import org.specs2.matcher.{MatchResult, TryMatchers}
import org.specs2.mutable.Specification

class PrefSpec
  extends Specification
    with TryMatchers
    with MongoTestUtils {

  val prefDAO = new DbPrefs {}

  sequential



  prefDAO.deleteAll()


  "prefDAO" should {

    "preload itself with default rtb categories" in {
      prefDAO.preload() must beEqualTo(23)
    }
    "check that loaded instances are ordered and valid" in {
      val list = prefDAO.load()
        list(0) match {
          case x =>
            x.id must beEqualTo("IAB1")

        }
        list(22) match {
          case x =>
            x.id must beEqualTo("IAB23")
        }

    }
  }

  def fail(): MatchResult[Any] = 1 === 0
}
