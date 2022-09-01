package specs2a1_ads

import java.util.UUID

import common.AllTests

/**
  * Created by pakkio on 05/10/16.
  */
class Specs2a1_AdLaunch extends AllTests

  with Kar134_ads_test
  with Kar135_points_test
  with Kar136_messages_test
  with Kar166_interaction_test {


  var currentAccountId: Option[String] = None
  var points=0
  val POINTS = 31
  val currentApplicationId = getNewRandomID
  dbAds.preload(currentApplicationId,10)


}
