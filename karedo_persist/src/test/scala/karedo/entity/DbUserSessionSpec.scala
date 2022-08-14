package karedo.entity

import java.util.UUID

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
      test.insertNew(sessionId,r) must beRight
      val updated = r.copy(info = Some("extended"), ts_expire = r.ts_created.plusMinutes(20))
      test.update(sessionId,updated)
      test.getById(sessionId) match {
        case Right(x) => x.info must beSome("extended")
        case Left(x) => ko(x)
      }

    }
  }