package karedo.entity

import java.util.UUID

import org.specs2.matcher.TryMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserEmailSpec
  extends Specification
      with TryMatchers
      with MongoTestUtils {

    val test = new DbUserEmail {}

    sequential

    "userEmail should insert " in {
      val emailId = "pakkio@gmail.com"
      val acctId = UUID.randomUUID()
      val r = UserEmail(emailId, acctId, active = false)
      test.insertNew(emailId,r) must beSuccessfulTry
      val updated = r.copy(active = true)
      test.update(emailId,updated)
      val reread = test.getById(emailId)
      reread must beSome
      reread.get.active  must beTrue

    }
  }