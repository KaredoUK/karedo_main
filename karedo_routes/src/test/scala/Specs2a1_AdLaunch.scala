import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.entity.dao.MongoConnection
import karedo.entity.{DbUserAd, DbUserApp, UserAd}
import karedo.routes.Routes
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsObject

import scala.concurrent.duration.{span, _}

/**
  * Created by pakkio on 05/10/16.
  */
class Specs2a1_AdLaunch extends WordSpec
  with MongoConnection
  with Routes
  with ScalatestRouteTest

  with Matchers {

  implicit val timeout = RouteTestTimeout(1000.second(span))

  // clear everything for tests to be understandable
  mongoClient.dropDatabase(mongoDbName)
  val dbUserAds = new DbUserAd {}
  dbUserAds.deleteAll()


  override val dbUserApp = new DbUserApp {}
  dbUserApp.deleteAll()

  var currentAccountId: Option[String] = None
  val currentApplicationId = "app1"
  dbUserAds.preload(currentApplicationId,10)

  "The Rest service must implement following API" should {


    "* kar134 /account/0/ads" in {
      // this is also creating an application associated with a dummy user
      Get(s"/account/0/ads?p=$currentApplicationId") ~>
        routesWithLogging ~>
        check
      {
        responseAs[List[UserAd]] should have size (10)
        status.intValue() shouldEqual(201)
        val uapp = dbUserApp.find(currentApplicationId)
        uapp.isOK should be(true)
        currentAccountId = Some(uapp.get.account_id)
      }

    }
    s"* kar135 /account/${currentAccountId}/points" in {
      //GET /account/{account_id}/points
      if(currentAccountId.isEmpty)
        fail("can't test without a valid current account")
      Get(s"/account/${currentAccountId.get}/points?p=$currentApplicationId") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual(206) // ?????
        responseAs[String] should be("""{"app_karedos":"10"}""")
      }

    }

  }

}
