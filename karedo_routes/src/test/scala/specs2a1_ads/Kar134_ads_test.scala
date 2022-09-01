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
          status.intValue() shouldEqual (201)
          val uapp = dbUserApp.find(currentApplicationId)
          uapp.isOK should be(true)
          currentAccountId = Some(uapp.get.account_id)
        }

    }
    "* identify 'anonymous' with previous used user" in {
      Get(s"/account/0/ads?p=$currentApplicationId") ~>
        routesWithLogging ~> check {
        points+=POINTS
        status.intValue() shouldEqual (200)
      }
    }
    "* gets an identifiable error when asking for a non existent account" in {
      Get(s"/account/unknown/ads?p=$currentApplicationId)") ~>
        routesWithLogging ~> check {
        status.intValue() shouldEqual (201) // FIXME? is just for prototype?
        responseAs[AdResponse].ads should have size (10)
        //responseAs[String] should include("Can't get ads because of")
      }
    }
    "* gets OK is using existent account" in {
      Get(s"/account/$currentAccountId/ads?p=$currentApplicationId") ~>
        routesWithLogging ~> check {
        points+=POINTS

        status.intValue() shouldEqual (205)
        responseAs[AdResponse].ads should have size (10)
      }
    }
  }
}
