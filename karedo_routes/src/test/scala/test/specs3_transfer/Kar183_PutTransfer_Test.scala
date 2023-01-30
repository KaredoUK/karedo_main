package test.specs3_transfer

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.entity._
import org.scalatest.junit.JUnitRunner
import karedo.util.Util.now
import org.junit.runner.RunWith
import org.scalatest.Ignore



/**
  * Created by pakkio on 10/20/16.
  */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar183_PutTransfer_Test extends AllTests {


  case class TestSetup
  (s_acctId:String, s_appId: String, s_sessId: String
    , r_msisdn1:String, r_msisdn2: String
    , r_accId1: String, r_accId2:String )

  def setUpTestParms: TestSetup = {
    val s_acctId = "s_acctId_" + getNewRandomID
    val s_appId = "s_appId_" + getNewRandomID
    val s_sessId = "s_sessId_" + getNewRandomID

    val r_msisdn1 = "+447711060452"
    val r_msisdn2 = "+447825010428"

    val r_accId1 = "r_accId1_" + getNewRandomID
    val r_accId2 = "r_accId2_" + getNewRandomID




    // Set up All Auth.
    dbUserApp.insertNew(UserApp(s_appId,s_acctId))
    dbUserAccount.insertNew(UserAccount(s_acctId,password=Some("pippo")))
    dbUserSession.insertNew(UserSession(s_sessId, s_acctId, ts_created = now))
    dbUserKaredos.insertNew(UserKaredos(s_acctId, 20000 * APP_KAREDO_CONV))
    dbUserProfile.insertNew(UserProfile(s_acctId, Some("F"), Some("John"), Some("Doe") ))

    // Set up working receiver account.
    dbUserAccount.insertNew(UserAccount(r_accId1,password=Some("pippo")))
    dbUserMobile.insertNew(UserMobile(r_msisdn1, r_accId1, true))
    dbUserKaredos.insertNew(UserKaredos(r_accId1, 0 * APP_KAREDO_CONV))

    TestSetup (s_acctId, s_appId, s_sessId
      , r_msisdn1, r_msisdn2
      , r_accId1, r_accId2 )
  }

  var accountId: String = ""
  "Kar183_putTransfer" must {
    "* PUT /transfer - Sender account has no valid mobile 1" in {
      val t = setUpTestParms

      val request1 = put_TransferRequest(
        account_id = t.s_acctId,
        application_id = t.s_appId,
        session_id = t.s_sessId,
        app_karedos = 111,
        receiver = Receiver(
          first_name = "Chandan",
          last_name = "Rajah",
          msisdn = t.r_msisdn1
        )
      ).toJson.toString
      Put(s"/transfer",
        HttpEntity(ContentTypes.`application/json`, request1)) ~>
        routesWithLogging ~>
        check {
          println(status.reason())
          val res=response

          status.intValue() shouldEqual (HTTP_SERVER_ERROR_500)
        }
      // now we can check if it was created
    }

    "* PUT /transfer - Sender account has no valid mobile 2" in {
      val t = setUpTestParms

      dbUserAccount.update(UserAccount(t.s_acctId, mobile = List(
        Mobile(t.r_msisdn2, Some("CODE"), valid = true)
      )))

      val request2 = put_TransferRequest(
        account_id = t.s_acctId,
        application_id = t.s_appId,
        session_id = t.s_sessId,
        app_karedos = 111,
        receiver = Receiver (
          first_name = "Chandan",
          last_name = "Rajah",
          msisdn = t.r_msisdn1
        )
      ).toJson.toString
      Put(s"/transfer",
        HttpEntity(ContentTypes.`application/json`, request2)) ~>
        routesWithLogging ~>
        check {
          println(status.reason())
          status.intValue() shouldEqual (HTTP_OK_200)
        }
      // now we can check if it was created
    }

    "* PUT /transfer - Valid Sender + Receiver" in {
      val t = setUpTestParms

      dbUserMobile.insertNew(UserMobile(t.r_msisdn2, t.s_acctId, true))
      dbUserAccount.update(UserAccount(t.s_acctId, mobile = List(
        Mobile(t.r_msisdn2, Some("CODE"), valid = true, ts_validated = Some(now))
      )))

      val request = put_TransferRequest(
        account_id = t.s_acctId,
        application_id = t.s_appId,
        session_id = t.s_sessId,
        app_karedos = 111,
        receiver = Receiver (
          first_name = "Chandan",
          last_name = "Rajah",
          msisdn = t.r_msisdn1
        )
      ).toJson.toString
      Put(s"/transfer",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          println(status.reason())
          status.intValue() shouldEqual (HTTP_OK_200)
        }
      // now we can check if it was created
    }

    "* PUT /transfer - Valid Sender but Receiver not joined" in {
      val t = setUpTestParms

      dbUserAccount.update(UserAccount(t.s_acctId, mobile = List(
        Mobile(t.r_msisdn1, Some("SPLAT"), valid = true, ts_validated = Some(now))
      )))

      val request = put_TransferRequest(
        account_id = t.s_acctId,
        application_id = t.s_appId,
        session_id = t.s_sessId,
        app_karedos = 111,
        receiver = Receiver (
          first_name = "Neetasha",
          last_name = "Rozario",
          msisdn = t.r_msisdn2
        )
      ).toJson.toString
      Put(s"/transfer",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          println(status.reason())
          status.intValue() shouldEqual (HTTP_OK_200)
        }
      // now we can check if it was created
    }
  }

}
