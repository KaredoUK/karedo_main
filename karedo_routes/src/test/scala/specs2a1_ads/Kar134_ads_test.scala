package specs2a1_ads

import common.AllTests
import karedo.rtb.model.AdModel.AdResponse
import karedo.util.Util
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.{JUnit3Suite, JUnitRunner}

/**
  * Created by pakkio on 10/21/16.
  */
@RunWith(classOf[JUnitRunner])
class Kar134_ads_test extends AllTests {
  "Kar134_ads" should {


    val newApp=Util.newMD5

    "* create a new account with anonymous access first time /account/0/ads" in {
      // this is also creating an application associated with a dummy user
      Get(s"/account/0/ads?p=$newApp") ~>
        routesWithLogging ~>
        check {
          implicit val json = jsonFormat2(AdResponse)
          responseAs[AdResponse].ads should have size (10)
          status.intValue() shouldEqual (HTTP_OK_CREATED_201)
          val uapp = dbUserApp.find(presetAppId)
          uapp.isOK should be(true)
          currentAccountId = uapp.get.account_id
        }

    }
    "* identify 'anonymous' with previous used user" in {
      Get(s"/account/0/ads?p=$presetAppId") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_OK_200)
      }
    }
    "* gets a descriptive error when asking for a non existent account" in {
      Get(s"/account/unknown/ads?p=$presetAppId)") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_NOTFOUND_404)
        responseAs[ErrorRes].error_text should include regex("No record found in table .*UserAccount")
      }
    }
    "* gets OK if using existent account" in {
      Get(s"/account/$presetAccount/ads?p=$presetAppId") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
        responseAs[AdResponse].ads should have size (10)
      }
    }
  }
}
