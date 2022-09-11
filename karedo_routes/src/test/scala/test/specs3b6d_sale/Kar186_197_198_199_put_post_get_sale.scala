package test.specs3b6d_sale

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.entity._
import karedo.util.Util.now
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


/**
  * Created by pakkio on 10/20/16.
  */
@RunWith(classOf[JUnitRunner])
class Kar186_197_198_199_put_post_get_sale extends AllTests {


  case class TestSetup
  (s_acctId:String, s_appId: String, s_sessId: String, s_msisdn: String, s_name: String,
   r_acctId:String, r_appId: String, r_sessId: String, r_msisdn: String, r_name: String )

  def setUpTestParms: TestSetup = {
    val s_acctId = "s_acctId_" + getNewRandomID
    val s_appId = "s_appId_" + getNewRandomID
    val s_sessId = "s_sessId_" + getNewRandomID
    val s_msisdn = "+447711060452"
    val s_l_name = "Doe"
    val s_f_name = "John"
    val s_name = s"$s_l_name, $s_f_name"

    val r_acctId = "r_acctId_" + getNewRandomID
    val r_appId = "r_appId_" + getNewRandomID
    val r_sessId = "r_sessId_" + getNewRandomID
    val r_msisdn = "+447825010428"
    val r_l_name = "Beeblebrox"
    val r_f_name = "Zaphod"
    val r_name = s"$r_l_name, $r_f_name"


    // Set up All Auth.
    dbUserApp.insertNew(UserApp(s_appId,s_acctId))
    dbUserAccount.insertNew(UserAccount(s_acctId,password=Some("sender"),
      mobile = List(Mobile(s_msisdn, Some("CODE"), true, now, Some(now)))))
    dbUserSession.insertNew(UserSession(s_sessId, s_acctId, ts_created = now))
    dbUserKaredos.insertNew(UserKaredos(s_acctId, 20000 * APP_KAREDO_CONV))
    dbUserProfile.insertNew(UserProfile(s_acctId, Some("F"), s_f_name, s_l_name ))
    dbUserMobile.insertNew(UserMobile(s_msisdn, s_acctId, true, now))

    // Set up working receiver account.
    dbUserApp.insertNew(UserApp(r_appId,r_acctId))
    dbUserAccount.insertNew(UserAccount(r_acctId,password=Some("receiver"),
      mobile = List(Mobile(r_msisdn, Some("CODE"), true, now, Some(now)))))
    dbUserSession.insertNew(UserSession(r_sessId, r_acctId, ts_created = now))
    dbUserKaredos.insertNew(UserKaredos(r_acctId, 20000 * APP_KAREDO_CONV))
    dbUserProfile.insertNew(UserProfile(r_acctId, Some("F"), r_f_name, r_l_name ))
    dbUserMobile.insertNew(UserMobile(r_msisdn, r_acctId, true, now))

    TestSetup (s_acctId, s_appId, s_sessId, s_msisdn, s_name,
      r_acctId, r_appId, r_sessId, r_msisdn, r_name )
  }

  var accountId: String = ""
  "Kar197_putSale" must {
    "* PUT /sale" in {
      val t = setUpTestParms

      val app_karedos = 100

      val request1 = Kar197Req(t.r_acctId, t.r_appId, t.r_sessId, app_karedos).toJson.toString

      Put(s"/sale",
        HttpEntity(ContentTypes.`application/json`, request1)) ~>
        routesWithLogging ~>
        check {
          println(status.reason())
          val res = response

          status.intValue() shouldEqual (HTTP_OK_200)

          val r = responseAs[Kar197Res]

          val sale = dbSale.find(r.sale_id).get

          sale.receiver_id should equal(t.r_acctId)
          sale.receiver_name should equal(t.r_name)
          sale.receiver_msisdn should equal(t.r_msisdn)

            Get(s"/sale/${r.sale_id}?a=${t.s_acctId}&p=${t.s_appId}&s=${t.s_sessId}") ~>
              routesWithLogging ~>
              check {
                val res = response
                status.intValue() shouldEqual (HTTP_OK_200)

                val sg = responseAs[Sale]

                sale.receiver_id should equal(sg.receiver_id)
                sale.receiver_name should equal(sg.receiver_name)
                sale.receiver_msisdn should equal(sg.receiver_msisdn)
                sale.status should equal(sg.status)
                sale.sale_type should equal(sg.sale_type)
                sale.karedos should equal(sg.karedos)
              }

            val reqS = Kar186Req(t.s_acctId, t.s_appId, t.s_sessId).toJson.toString
            Post(s"/sale/${r.sale_id}/complete",
              HttpEntity(ContentTypes.`application/json`, reqS)) ~>
              routesWithLogging ~>
              check {
                val res = response
                status.intValue() shouldEqual (HTTP_OK_200)

                val s_o = dbSale.find(r.sale_id).get

                s_o.sender_id should equal(t.s_acctId)
                s_o.sender_name should equal(t.s_name)
                s_o.sender_msisdn should equal(t.s_msisdn)

                  Get(s"/sale/${r.sale_id}?a=${t.s_acctId}&p=${t.s_appId}&s=${t.s_sessId}") ~>
                    routesWithLogging ~>
                    check {
                      val res = response
                      status.intValue() shouldEqual (HTTP_OK_200)

                      val sg = responseAs[Sale]

                      s_o.receiver_id should equal(sg.receiver_id)
                      s_o.receiver_name should equal(sg.receiver_name)
                      s_o.receiver_msisdn should equal(sg.receiver_msisdn)
                      s_o.status should equal(sg.status)
                      s_o.sale_type should equal(sg.sale_type)
                      s_o.karedos should equal(sg.karedos)
                      s_o.sender_id should equal(sg.sender_id)
                      s_o.sender_name should equal(sg.sender_name)
                      s_o.sender_msisdn should equal(sg.sender_msisdn)
                    }
              }
        }

        val uk_s = dbUserKaredos.find(t.s_acctId).get
        val uk_r = dbUserKaredos.find(t.r_acctId).get

        (uk_r.karedos - uk_s.karedos) should equal(app_karedos * 2 * APP_KAREDO_CONV)
    }
  }

}
