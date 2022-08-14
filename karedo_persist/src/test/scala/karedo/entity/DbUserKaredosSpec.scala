package karedo.entity

import java.util.UUID

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
      test.insertNew(acctId,r) must beRight
      val updated = r.copy(karedos = 2000)
      test.update(acctId,updated)
      test.getById(acctId) match {
        case Right(x) => x.karedos must beEqualTo(2000)
        case Left(x) => ko(x)
      }

    }
  }