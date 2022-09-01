package specs6_intent

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

/**
  * Created by pakkio on 10/21/16.
  */
trait Kar171_putIntent_test {
  self : Specs6c_Intent =>

  "Kar171" should {
    "PUT /account/{{account_id}}/intent" in {
      val request = Kar170Req(application_id, session_id, Kar170ReqIntentUnit("why_00", "what_00", "when_00", "where_00")).toJson.toString

      Put(s"/account/$account_id/intent",
        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~> check {
        // @TODO: Check this more.
        val st = status.intValue()
        val res = response
        st shouldEqual (205)
      }
    }
  }
}
