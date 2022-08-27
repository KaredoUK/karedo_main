package karedo.entity

import java.util.UUID

import karedo.util.{KO, OK}
import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserPrefsSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserPrefs {}
    test.deleteAll()

    sequential

    "userPrefs should insert " in {
      val list: Map[String,Double] = Map(("IAB1",5), ("IAB2",7))
      val list2: Map[String, Double] = Map(("IAB1",4), ("IAB2",7))
      val acctId = UUID.randomUUID()
      val r = UserPrefs(acctId, list)
      test.insertNew(r) must beOK
      val updated = r.copy(prefs = list2)
      test.update(updated)
      test.find(acctId) match {
        case OK(x) => x.prefs must contain(("IAB1",4.0))
        case KO(x) => ko(x)
      }

    }
  }