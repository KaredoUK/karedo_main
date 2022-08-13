package karedo.entity

import java.util.UUID

import org.specs2.matcher.TryMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserKaredosSpec
  extends Specification
      with TryMatchers
      with MongoTestUtils {

    val test = new DbUserKaredos {}
    test.deleteAll()

    sequential

    "userKaredos should insert " in {
      val acctId = UUID.randomUUID()
      val r = UserKaredos(acctId, 5000)
      test.insertNew(acctId,r) must beSuccessfulTry
      val updated = r.copy(karedos = 2000)
      test.update(acctId,updated)
      val reread = test.getById(acctId)
      reread must beSome
      reread.get.karedos  must beEqualTo(2000)

    }
  }