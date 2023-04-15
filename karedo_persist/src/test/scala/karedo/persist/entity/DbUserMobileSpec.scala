package karedo.persist.entity

import java.util.UUID

import karedo.common.misc.Util
import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import karedo.common.result.{Result, OK, KO}

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

    "userMobile in findByAccount" in {
      val mobile = Util.newMD5
      val acctId = UUID.randomUUID()
      val r = UserMobile(mobile, acctId, active = false)

      test.insertNew(r)
      val m = test.find(mobile).get
      m.id shouldEqual r.id
      m.account_id shouldEqual r.account_id
      m.active shouldEqual r.active

      val ums = test.findByAccount(acctId).get
      ums should not equalTo(Nil)
      ums should not equalTo(List())
      ums.size shouldEqual 1

      val ms = ums.head
      ms.id shouldEqual r.id
      ms.account_id shouldEqual r.account_id
      ms.active shouldEqual r.active
    }
  }