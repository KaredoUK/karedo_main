package karedo.persist.entity

import java.util.UUID

import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import karedo.common.result.{Result, OK, KO}

@RunWith(classOf[JUnitRunner])
class DbUserProfileSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserProfile {}
    //test.deleteAll()

    sequential

    "userprofile should insert " in {
      val acctId = UUID.randomUUID()
      val appId = UUID.randomUUID()
      val r = UserProfile(appId, Some("M"), Some("Claudio"), Some("Pacchiega"))
      test.insertNew(r) must beOK
      val updated = r.copy(yob = Some(1962))
      test.update(updated)
      test.find(appId) match {
        case OK(x) => x.yob must beEqualTo(Some(1962))
        case KO(x) => ko(x)
      }

    }
  }