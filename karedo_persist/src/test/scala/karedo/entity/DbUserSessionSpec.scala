package karedo.entity

import java.util.UUID

import karedo.util.{KO, OK}
import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DbUserSessionSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserSession {}
    //test.deleteAll()

    sequential

    "usersession should insert " in {
      val acctId = UUID.randomUUID()
      val sessionId = UUID.randomUUID()
      val r = UserSession(sessionId, acctId)
      test.insertNew(r) must beOK
      val updated = r.copy(info = Some("extended"), ts_expire = r.ts_created.plusMinutes(20))
      test.update(updated)
      test.find(sessionId) match {
        case OK(x) => x.info must beSome("extended")
        case KO(x) => ko(x)
      }

    }
  }