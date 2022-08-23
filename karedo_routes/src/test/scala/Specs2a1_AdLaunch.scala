import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.entity.dao.MongoConnection
import karedo.entity._
import karedo.routes.Routes
import karedo.util.KaredoJsonHelpers
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
  with DbCollections
  with KaredoJsonHelpers

  with Matchers {

  implicit val timeout = RouteTestTimeout(1000.second(span))

  // clear everything for tests to be understandable
  mongoClient.dropDatabase(mongoDbName)
  val dbUserAds = new DbUserAd {}
  dbUserAds.deleteAll()


  override val dbUserApp = new DbUserApp {}
  dbUserApp.deleteAll()

  var currentAccountId: Option[String] = None
  var points=0
  val POINTS = 31
  val currentApplicationId = "app1"
  dbAds.preload(currentApplicationId,10)

  "Kar134" should {


    "* create a new account with anonymous access first time /account/0/ads" in {
      // this is also creating an application associated with a dummy user
      Get(s"/account/0/ads?p=$currentApplicationId") ~>
        routesWithLogging ~>
        check {
          points+=POINTS
          responseAs[List[Ad]] should have size (10)
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
        status.intValue() shouldEqual (500)
        responseAs[String] should include("Can't get ads because of")
        }
    }
    "* gets OK is using existent account" in {
      Get(s"/account/$currentAccountId/ads?p=$currentApplicationId") ~>
        routesWithLogging ~> check {
        points+=POINTS

        status.intValue() shouldEqual (205)
        responseAs[List[Ad]] should have size (10)
      }
    }
  }
  "Kar135" should {
    s"* get points as anonymous /account/${currentAccountId}/points" in {
      //GET /account/{account_id}/points
      if(currentAccountId.isEmpty)
        fail("can't test without a valid current account")
      Get(s"/account/${currentAccountId.get}/points?p=$currentApplicationId") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual(206) // ?????
        val q="\""
        val json = s"{${q}app_karedos${q}:${q}$points${q}}"
        responseAs[String] should be(json)
      }

    }
    "* get points as an invalid applicationId should fail with explanation " in {
      Get(s"/account/unknown/points?p=unknown") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual (500) // ?????
        responseAs[String] should include("No record found in table XUserKaredos")
      }
    }

  }
  "Kar136" should {
    "* get messages even if empty" in {
      Get(s"/account/${currentAccountId.get}/messages?p=$currentApplicationId") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual (206)
        responseAs[List[UserMessages]] should be(List())
      }
    }
  }

}
