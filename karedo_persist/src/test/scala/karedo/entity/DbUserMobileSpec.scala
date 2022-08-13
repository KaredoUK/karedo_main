package karedo.entity

import java.util.UUID

import org.specs2.matcher.TryMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserMobileSpec
  extends Specification
      with TryMatchers
      with MongoTestUtils {

    val test = new DbUserMobile {}
    test.deleteAll()

    sequential

    "userMobile should insert " in {
      val mobile = "12345678"
      val acctId = UUID.randomUUID()
      val r = UserMobile(mobile, acctId, active = false)
      test.insertNew(mobile,r) must beSuccessfulTry
      val updated = r.copy(active = true)
      test.update(mobile,updated)
      val reread = test.getById(mobile)
      reread must beSome
      reread.get.active  must beTrue

    }
  }