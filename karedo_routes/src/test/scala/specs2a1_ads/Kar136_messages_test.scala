package specs2a1_ads

import karedo.entity.UserMessages

/**
  * Created by pakkio on 10/21/16.
  */
trait Kar136_messages_test {
  self: Specs2a1_AdLaunch =>

  "Kar136_messages" should {
    "* get messages even if empty" in {
      Get(s"/account/${currentAccountId.get}/messages?p=$currentApplicationId") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_OK_PARTIALCONTENT_206)
        responseAs[List[UserMessages]] should be(List())
      }
    }
  }

}
