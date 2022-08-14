package karedo.entity

import java.util.UUID

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
      test.insertNew(mobile,r) must beRight
      val updated = r.copy(active = true)
      test.update(mobile,updated)
      test.getById(mobile) match {
        case Right(x) => x.active must beTrue
        case Left(x) => ko(x)
      }

    }
  }