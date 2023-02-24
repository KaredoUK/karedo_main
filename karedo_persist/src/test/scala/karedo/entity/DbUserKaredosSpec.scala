package karedo.entity

import java.util.{Random, UUID}

import karedo.util.{KO, OK, Result}
import org.junit.Ignore
import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@Ignore
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
  private val ACCT1START = 1000
  private val ACCT2START = 500
  "can transfer funds from a user to another" in {
    val acct1 = UUID.randomUUID()
    val acct2 = UUID.randomUUID()

    test.insertNew(UserKaredos(acct1,ACCT1START))
    test.insertNew(UserKaredos(acct2,ACCT2START))
    test.transferKaredo(acct1,acct2,100,"TRANSFER")

    test.find(acct1) match {
      case OK(x) => x.karedos must beEqualTo(900)
      case KO(x) => ko(x)
    }
    test.find(acct2) match {
      case OK(x) => x.karedos must beEqualTo(600)
      case KO(x) => ko(x)
    }

  }

  "can we lock" in {
    val acct1 = UUID.randomUUID()
    test.insertNew(UserKaredos(acct1,0))
    val locked = test.lock(acct1,"transid")
    locked must beOK
    val locked1 = test.lock(acct1,"transid2",retries = 5)
    locked1 must beKO
    val unlock = test.unlock(acct1,"transid")
    unlock must beOK
    val locked2 = test.lock(acct1,"transid2")
    locked2 must beOK
    val lockedinvalid = test.lock(UUID.randomUUID(),"transid2"  )
    lockedinvalid must beKO
  }

  "can we transfer safely? (10 threads each inserting and removing same random quantities should keep original balance)" in {
    //transferWithFunction(test.transferKaredo)
    val acct1 = UUID.randomUUID()
    val acct2 = UUID.randomUUID()

    test.insertNew(UserKaredos(acct1,ACCT1START))
    test.insertNew(UserKaredos(acct2,ACCT2START))

    // launch 1000 transfers which should not alter the initial amount
    val transferType = "TRANSFER"
    val futures = for{ i <- 1 to 10}
      yield Future {
        for (j <- 1 to 10) {
          var kar = new Random().nextInt(100000)
          //println(s"$i/$j transferring 1")
          test.transferKaredo(acct1, acct2, kar, transferType)
          //println(s"$i/$j transferring 2")
          test.transferKaredo(acct2, acct1, kar, transferType)
        }
      }

    val future = Future.fold(futures.toList)(List[Unit]())((acc, e) => e :: acc)

    Await.result(future, 100 seconds)
    test.find(acct1) match {
      case OK(x) => x.karedos must beEqualTo(ACCT1START)
      case KO(x) => ko(x)
    }
    test.find(acct2) match {
      case OK(x) => x.karedos must beEqualTo(ACCT2START)
      case KO(x) => ko(x)
    }
  }
//  "naive should fail (10 threads with +- random quantities should alter balance" in {
//    Try { transferWithFunction(test.transferKaredoNaive) }
//    match {
//      case Success(_) => ko("should have produced an error")
//      case Failure(x) => {
//        val s = s"it's ok error produced is $x"
//        println(s)
//        ok(s)
//      }
//    }
//  }
}