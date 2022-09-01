package specs2a1_ads

/**
  * Created by pakkio on 10/21/16.
  */
trait Kar135_points_test {
  self : Specs2a1_AdLaunch =>
  "Kar135_points" should {
    s"* get points as anonymous /account/${currentAccountId}/points" in {
      //GET /account/{account_id}/points
      if(currentAccountId.isEmpty)
        fail("can't test without a valid current account")
      Get(s"/account/${currentAccountId.get}/points?p=$currentApplicationId") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual(206) // ?????

        case class AppKaredos(app_karedos: String)
        implicit val json = jsonFormat1(AppKaredos)
        responseAs[Kar135Res].app_karedos.toInt should be > 35

      }

    }
    "* get points as an invalid applicationId should fail with explanation " in {
      Get(s"/account/unknown/points?p=unknown") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual (500) // ?????
        responseAs[String] should include("No record found in table XUserKaredos")
      }
    }

  }
}
