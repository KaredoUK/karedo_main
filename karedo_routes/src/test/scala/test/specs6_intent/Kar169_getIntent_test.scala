package test.specs6_intent

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.entity.{UserAccount, UserApp, UserIntent}
import karedo.util.Util
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.scalatest.junit.JUnitRunner

/**
  * Created by pakkio on 10/21/16.
  */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar169_getIntent_test extends AllTests {

  val presetAppId = Util.newMD5
  val presetAccount = Util.newUUID

  dbUserAccount.insertNew(UserAccount(presetAccount))
  dbUserApp.insertNew(UserApp(presetAppId,presetAccount))
  val uuids:Array[String] = Array(getNewRandomID, getNewRandomID, getNewRandomID)


  "Kar169 No Intent ID" should {
    "GET /account/{{account_id}}/intent/{{intent_id}}" in {
      Get(s"/account/$presetAccount/intent/0?p=$presetAppId") ~>
        routesWithLogging ~>
        check {
          val userIntent = responseAs[UserIntent]
          status.intValue() shouldEqual (HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
          userIntent.intents shouldEqual List()
        }
    }
  }
  "Kar169 with Intent ID" should {
    "GET /account/{{account_id}}/intent/{{intent_id}}" in {

      val request = IntentUpdateRequest(presetAppId, session_id=Util.newUUID, IntentUnitRequest("why_01", "what_01", "when_00", "where_00")).toJson.toString
      Put(s"/account/$presetAccount/intent",
        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~>
      check {
        val res = response
        status.intValue() shouldEqual(HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
      }

      Get(s"/account/$presetAccount/intent/0?p=$presetAppId") ~>
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
