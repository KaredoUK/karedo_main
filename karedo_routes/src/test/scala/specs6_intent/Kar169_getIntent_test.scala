package specs6_intent

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import karedo.entity.UserIntent

/**
  * Created by pakkio on 10/21/16.
  */
trait Kar169_getIntent_test {
  self : Specs6c_Intent =>

  "Kar169 No Intent ID" should {
    "GET /account/{{account_id}}/intent/{{intent_id}}" in {
      Get(s"/account/$account_id/intent/0?p=$application_id") ~>
        routesWithLogging ~>
        check {
          val userIntent = responseAs[UserIntent]
          status.intValue() shouldEqual (201)
          userIntent.intents shouldEqual List()
        }
    }
  }
  "Kar169 with Intent ID" should {
    "GET /account/{{account_id}}/intent/{{intent_id}}" in {

      val request = Kar170Req(application_id, session_id, Kar170ReqIntentUnit("why_01", "what_01", "when_00", "where_00")).toJson.toString
      Put(s"/account/$account_id/intent",
        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~>
      check {
        val response = Kar170
      }

      Get(s"/account/$account_id/intent/0?p=$application_id") ~>
        routesWithLogging ~>
        check {
          val userIntent = responseAs[UserIntent]
          userIntent.intents should have size(1)
          val intent = userIntent.intents(0)
          intent.what shouldEqual("what_01")
        }
    }
  }
}
