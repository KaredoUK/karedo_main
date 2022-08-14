package karedo.entity

import java.util.UUID

import org.specs2.matcher.{EitherMatchers, TryMatchers}
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserProfileSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserProfile {}
    test.deleteAll()

    sequential

    "userprofile should insert " in {
      val acctId = UUID.randomUUID()
      val appId = UUID.randomUUID()
      val r = UserProfile(appId, "M", "Claudio", "Pacchiega")
      test.insertNew(appId,r) must beRight
      val updated = r.copy(yob = Some(1962))
      test.update(appId,updated)
      test.getById(appId) match {
        case Right(x) => x.yob must beEqualTo(Some(1962))
        case Left(x) => ko(x)
      }

    }
  }