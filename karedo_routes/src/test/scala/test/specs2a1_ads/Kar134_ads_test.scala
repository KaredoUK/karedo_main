package test.specs2a1_ads

import common.AllTests
import karedo.common.misc.Util
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.rtb.model.AdModel.AdResponse
import org.junit.runner.RunWith
import org.scalatest.{Ignore, WordSpec}
import org.scalatest.junit.{JUnit3Suite, JUnitRunner}
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections

/**
  * Created by pakkio on 10/21/16.
  */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar134_ads_test extends AllTests {
  val presetAppId = Util.newMD5
  val presetAccount = Util.newUUID
  val count = 1

  dbUserAccount.insertNew(UserAccount(presetAccount))
  dbUserApp.insertNew(UserApp(presetAppId,presetAccount))

  "Kar134_ads" should {


    val newApp=Util.newMD5
    var currentAccountId = ""

    "* create a new account with anonymous access first time /account/0/ads" in {
      // this is also creating an application associated with a dummy user
      Get(s"/account/0/ads?p=$newApp&c=$count") ~>
        routesWithLogging ~>
        check {
          responseAs[AdResponse].ads should have size (count)
          status.intValue() shouldEqual (HTTP_OK_CREATED_201)
          val uapp = dbUserApp.find(presetAppId)
          uapp.isOK should be(true)
          currentAccountId = uapp.get.account_id
        }

    }
    "* identify 'anonymous' with previous used user" in {
      Get(s"/account/0/ads?p=$presetAppId&c=$count") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_OK_200)
      }
    }
    "* gets a descriptive error when asking for a non existent account" in {
      Get(s"/account/unknown/ads?p=$presetAppId&c=$count") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_NOTFOUND_404)
        responseAs[ErrorRes].error_text should include regex("No record found in table .*UserAccount")
      }
    }
    "* gets OK if using existent account" in {
      Get(s"/account/$presetAccount/ads?p=$presetAppId&c=$count") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
        responseAs[AdResponse].ads should have size (count)
      }
    }
  }
}
