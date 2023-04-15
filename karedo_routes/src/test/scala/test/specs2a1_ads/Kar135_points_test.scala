package test.specs2a1_ads

import common.AllTests
import karedo.common.misc.Util
import karedo.persist.entity.{UserAccount, UserApp, UserKaredos}
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.scalatest.junit.JUnitRunner
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections

/**
  * Created by pakkio on 10/21/16.
  */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar135_points_test extends AllTests {
  "Kar135_points" should {

    val KAREDO_AMOUNT = 2718281
    val presetAppId = Util.newMD5
    val presetAccount = Util.newUUID

    dbUserAccount.insertNew(UserAccount(presetAccount))
    dbUserApp.insertNew(UserApp(presetAppId,presetAccount))
    dbUserKaredos.insertNew(UserKaredos(presetAccount,KAREDO_AMOUNT))

    s"* get points as anonymous /account/${presetAccount}/points" in {
      //GET /account/{account_id}/points
      Get(s"/account/$presetAccount/points?p=$presetAppId") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual(206)

        case class AppKaredos(app_karedos: String)
        implicit val json = jsonFormat1(AppKaredos)

        val res = response
        responseAs[KaredosResponse].app_karedos should equal(KAREDO_AMOUNT / APP_KAREDO_CONV)

      }

    }
    "* get points as an invalid applicationId should fail with explanation " in {
      Get(s"/account/unknown/points?p=unknown") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_BADREQUEST_400) // ?????
        val res = responseAs[ErrorRes]
        res.error_code shouldEqual HTTP_BADREQUEST_400
      }
    }

  }
}
