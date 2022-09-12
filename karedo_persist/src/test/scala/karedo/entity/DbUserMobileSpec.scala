package karedo.entity

import java.util.UUID

import karedo.util.{KO, OK, Util}
import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DbUserMobileSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserMobile {}
    //test.deleteAll()

    sequential

    "userMobile should insert " in {
      val mobile = Util.newMD5
      val acctId = UUID.randomUUID()
      val r = UserMobile(mobile, acctId, active = false)
      test.insertNew(r) must beOK
      val updated = r.copy(active = true)
      test.update(updated)
      test.find(mobile) match {
        case OK(x) => x.active must beTrue
        case KO(x) => ko(x)
      }

    }
  }