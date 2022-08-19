package karedo.entity

import java.util.UUID

import karedo.entity.dao.{KO, OK}
import org.specs2.matcher.{EitherMatchers, TryMatchers}
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserEmailSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserEmail {}
    test.deleteAll()

    sequential

    "userEmail should insert " in {
      val emailId = "pakkio@gmail.com"
      val acctId = UUID.randomUUID()
      val r = UserEmail(emailId, acctId, active = false)
      test.insertNew(r) must beOK
      val updated = r.copy(active = true)
      test.update(updated)
      test.getById(emailId) match {
        case OK(x) => x.active must beTrue
        case KO(x) => ko(x)
      }

    }
  }