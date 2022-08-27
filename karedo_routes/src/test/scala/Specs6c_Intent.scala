import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.entity.dao.MongoConnection
import karedo.entity._
import karedo.routes.Routes
import karedo.rtb.model.AdModel.AdResponse
import karedo.util.KaredoJsonHelpers
import org.scalatest.{Matchers, WordSpec}
import spray.json.JsObject
import karedo.util._
import java.util.UUID

import scala.concurrent.duration.{span, _}

/**
  * Created by crajah on 15/10/2016.
  */
class Specs6c_Intent extends WordSpec
  with MongoConnection
  with Routes
  with ScalatestRouteTest
  with DbCollections
  with KaredoJsonHelpers
  with Matchers {

  implicit val timeout = RouteTestTimeout(1000.second(span))

  // clear everything for tests to be understandable
  mongoClient.dropDatabase(mongoDbName)
  dbUserIntent.deleteAll()
  val uuids:Array[String] = Array(UUID.randomUUID().toString, UUID.randomUUID().toString, UUID.randomUUID().toString)

  val account_id = UUID.randomUUID().toString
  val application_id = UUID.randomUUID().toString
  val session_id = UUID.randomUUID().toString

  "Kar169 No Intent ID" should {
    "GET /account/{{account_id}}/intent/{{intent_id}}" in {
      Get(s"/account/$account_id/intent/0?p=$application_id") ~>
        routesWithLogging ~>
        check {
          val userIntent = responseAs[UserIntent]
          userIntent.intents shouldEqual Nil
        }
    }
  }

  "Kar171" should {
    "PUT /account/{{account_id}}/intent" in {
      val request = Kar170Req(application_id, session_id, Kar170ReqIntentUnit("why_00", "what_00", "when_00", "where_00")).toJson.toString

      Put(s"/account/$account_id/intent",
        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~> check {
        status.intValue() shouldEqual (200)
      }
    }
  }

  "Kar169 with Intent ID" should {
    "GET /account/{{account_id}}/intent/{{intent_id}}" in {
      Get(s"/account/$account_id/intent/0?p=$application_id") ~>
        routesWithLogging ~>
        check {
          val userIntent = responseAs[UserIntent]
          userIntent.intents should have size(1)
        }
    }
  }


}
