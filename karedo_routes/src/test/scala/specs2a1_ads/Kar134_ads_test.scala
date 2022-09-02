package specs2a1_ads

import karedo.rtb.model.AdModel.AdResponse

/**
  * Created by pakkio on 10/21/16.
  */
trait Kar134_ads_test {
  self: Specs2a1_AdLaunch =>
  "Kar134_ads" should {


    "* create a new account with anonymous access first time /account/0/ads" in {
      // this is also creating an application associated with a dummy user
      Get(s"/account/0/ads?p=$currentApplicationId") ~>
        routesWithLogging ~>
        check {
          points+=POINTS
          implicit val json = jsonFormat2(AdResponse)
          responseAs[AdResponse].ads should have size (10)
          status.intValue() shouldEqual (HTTP_OK_CREATED_201)
          val uapp = dbUserApp.find(currentApplicationId)
          uapp.isOK should be(true)
          currentAccountId = Some(uapp.get.account_id)
          1===1
        }

    }
    "* identify 'anonymous' with previous used user" in {
      Get(s"/account/0/ads?p=$currentApplicationId") ~>
        routesWithLogging ~> check {
        points+=POINTS
        status.intValue() shouldEqual (HTTP_OK_200)
      }
    }
    "* gets a descriptive error when asking for a non existent account" in {
      Get(s"/account/unknown/ads?p=$currentApplicationId)") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_NOTFOUND_404)
        responseAs[ErrorRes].error_text should include ("No record found in table XUserAccount")
      }
    }
    "* gets OK is using existent account" in {
      Get(s"/account/${currentAccountId.get}/ads?p=$currentApplicationId") ~>
        routesWithLogging ~> check {
        points+=POINTS

        status.intValue() shouldEqual (HTTP_OK_PARTIALCONTENT_206)
        responseAs[AdResponse].ads should have size (10)
      }
    }
  }
}
