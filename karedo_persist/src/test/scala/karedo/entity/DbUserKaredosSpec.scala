package karedo.entity

import java.util.UUID

import karedo.util.{KO, OK}
import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DbUserKaredosSpec
  extends Specification
    with EitherMatchers
    with MongoTestUtils {

  val test = new DbUserKaredos {}
  //test.deleteAll()

  sequential

  "userKaredos should insert " in {
    val acctId = UUID.randomUUID()
    val r = UserKaredos(acctId, 5000)
    test.insertNew(r) must beOK
    val updated = r.copy(karedos = 2000)
    test.update(updated)
    test.find(acctId) match {
      case OK(x) => x.karedos must beEqualTo(2000)
      case KO(x) => ko(x)
    }
    test.addKaredos(acctId, 111) must beOK
    test.find(acctId) match {
      case OK(x) => x.karedos must beEqualTo(2111)
      case KO(x) => ko(x)
    }

  }
  "can transfer funds from a user to another" in {
    val acct1 = UUID.randomUUID()
    val acct2 = UUID.randomUUID()

    test.insertNew(UserKaredos(acct1,1000))
    test.insertNew(UserKaredos(acct2,500))
    test.transferKaredo(acct1,acct2,100)

    test.find(acct1) match {
      case OK(x) => x.karedos must beEqualTo(900)
      case KO(x) => ko(x)
    }
    test.find(acct2) match {
      case OK(x) => x.karedos must beEqualTo(600)
      case KO(x) => ko(x)
    }

  }
}