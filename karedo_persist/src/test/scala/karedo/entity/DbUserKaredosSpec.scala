package karedo.entity

import java.util.UUID

import karedo.entity.dao.{KO, OK}
import org.specs2.matcher.{EitherMatchers, TryMatchers}
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserKaredosSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserKaredos {}
    test.deleteAll()

    sequential

    "userKaredos should insert " in {
      val acctId = UUID.randomUUID()
      val r = UserKaredos(acctId, 5000)
      test.insertNew(r) must beOK
      val updated = r.copy(karedos = 2000)
      test.update(updated)
      test.getById(acctId) match {
        case OK(x) => x.karedos must beEqualTo(2000)
        case KO(x) => ko(x)
      }

    }
  }