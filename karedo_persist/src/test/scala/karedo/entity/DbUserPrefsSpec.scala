package karedo.entity

import java.util.UUID

import karedo.entity.dao.{KO, OK}
import org.specs2.matcher.{EitherMatchers, TryMatchers}
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
      val list = List(UserPref("IAB1",5), UserPref("IAB2",7))
      val list2 = List(UserPref("IAB1",4), UserPref("IAB2",7))
      val acctId = UUID.randomUUID()
      val r = UserPrefs(acctId, list)
      test.insertNew(r) must beOK
      val updated = r.copy(prefs = list2)
      test.update(updated)
      test.find(acctId) match {
        case OK(x) => x.prefs.head.value must beEqualTo(4)
        case KO(x) => ko(x)
      }

    }
  }