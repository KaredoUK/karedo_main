package karedo.entity

import java.util.UUID

import karedo.entity.dao.{KO, OK}
import org.specs2.matcher.{EitherMatchers, TryMatchers}
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserMobileSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserMobile {}
    test.deleteAll()

    sequential

    "userMobile should insert " in {
      val mobile = "12345678"
      val acctId = UUID.randomUUID()
      val r = UserMobile(mobile, acctId, active = false)
      test.insertNew(r) must beOK
      val updated = r.copy(active = true)
      test.update(updated)
      test.getById(mobile) match {
        case OK(x) => x.active must beTrue
        case KO(x) => ko(x)
      }

    }
  }