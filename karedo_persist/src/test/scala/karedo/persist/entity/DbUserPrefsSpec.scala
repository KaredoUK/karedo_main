package karedo.persist.entity

import java.util.UUID

import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import karedo.common.result.{Result, OK, KO}

@RunWith(classOf[JUnitRunner])
class DbUserPrefsSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserPrefs {}
    //test.deleteAll()

    sequential

    "userPrefs should insert " in {
      val list: Map[String,UserPrefData] = Map(("IAB1", UserPrefData(5, "IAB1", 0)), ("IAB2", UserPrefData(7, "IAB2", 1)))
      val list2: Map[String, UserPrefData] = Map(("IAB1", UserPrefData(4, "IAB1", 0)), ("IAB2", UserPrefData(7, "IAB2", 1)))
      val acctId = UUID.randomUUID()
      val r = UserPrefs(acctId, list)
      test.insertNew(r) must beOK
      val updated = r.copy(prefs = list2)
      test.update(updated)
      test.find(acctId) match {
        case OK(x) => x.prefs must contain(("IAB1",UserPrefData(4.0, "IAB1", 0)))
        case KO(x) => ko(x)
      }

    }
  }