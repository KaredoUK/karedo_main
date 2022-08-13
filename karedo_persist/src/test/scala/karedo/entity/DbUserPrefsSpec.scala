package karedo.entity

import java.util.UUID

import org.specs2.matcher.TryMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserPrefsSpec
  extends Specification
      with TryMatchers
      with MongoTestUtils {

    val test = new DbUserPrefs {}

    sequential

    "userPrefs should insert " in {
      val list = List(UserPref("IAB1",5), UserPref("IAB2",7))
      val list2 = List(UserPref("IAB1",4), UserPref("IAB2",7))
      val acctId = UUID.randomUUID()
      val r = UserPrefs(acctId, list)
      test.insertNew(acctId,r) must beSuccessfulTry
      val updated = r.copy(prefs = list2)
      test.update(acctId,updated)
      val reread = test.getById(acctId)
      reread must beSome
      reread.get.prefs(0).value  must beEqualTo(4)

    }
  }