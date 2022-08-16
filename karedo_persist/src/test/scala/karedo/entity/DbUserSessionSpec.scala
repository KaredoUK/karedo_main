package karedo.entity

import java.util.UUID

import karedo.entity.dao.{KO, OK}
import org.specs2.matcher.{EitherMatchers, TryMatchers}
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserSessionSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserSession {}
    test.deleteAll()

    sequential

    "usersession should insert " in {
      val acctId = UUID.randomUUID()
      val sessionId = UUID.randomUUID()
      val r = UserSession(sessionId, acctId)
      test.insertNew(sessionId,r) must beOK
      val updated = r.copy(info = Some("extended"), ts_expire = r.ts_created.plusMinutes(20))
      test.update(sessionId,updated)
      test.getById(sessionId) match {
        case OK(x) => x.info must beSome("extended")
        case KO(x) => ko(x)
      }

    }
  }