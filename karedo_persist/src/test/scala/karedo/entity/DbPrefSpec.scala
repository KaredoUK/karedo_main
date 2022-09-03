package karedo.entity

import org.specs2.matcher.{MatchResult, TryMatchers}
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DbPrefSpec
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


}
