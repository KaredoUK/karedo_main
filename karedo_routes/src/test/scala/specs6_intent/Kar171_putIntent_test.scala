package specs6_intent

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.util.Util
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
  * Created by pakkio on 10/21/16.
  */
@RunWith(classOf[JUnitRunner])
class Kar171_putIntent_test extends AllTests {

  "Kar171" should {
    "PUT /account/{{account_id}}/intent" in {
      val request = Kar170Req(presetAccount, session_id=Util.newUUID, Kar170ReqIntentUnit("why_00", "what_00", "when_00", "where_00")).toJson.toString

      Put(s"/account/$presetAccount/intent",
        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~> check {
        // @TODO: Check this more.
        val st = status.intValue()
        val res = response
        st shouldEqual (HTTP_OK_CREATED_201)
      }
    }
  }
}
