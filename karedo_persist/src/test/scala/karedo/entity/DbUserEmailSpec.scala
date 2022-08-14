package karedo.entity

import java.util.UUID

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
      test.insertNew(emailId,r) must beRight
      val updated = r.copy(active = true)
      test.update(emailId,updated)
      test.getById(emailId) match {
        case Right(x) => x.active must beTrue
        case Left(x) => ko(x)
      }

    }
  }