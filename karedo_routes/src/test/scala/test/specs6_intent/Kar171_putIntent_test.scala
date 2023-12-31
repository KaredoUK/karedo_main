package test.specs6_intent

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.common.misc.Util
import karedo.persist.entity.{UserAccount, UserApp, UserSession}
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.scalatest.junit.JUnitRunner
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections

/**
  * Created by pakkio on 10/21/16.
  */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar171_putIntent_test extends AllTests {

  val presetAppId = Util.newMD5
  val presetAccount = Util.newUUID
  val presetSessId = Util.newUUID

  dbUserAccount.insertNew(UserAccount(presetAccount))
  dbUserApp.insertNew(UserApp(presetAppId,presetAccount))
  dbUserSession.insertNew(UserSession(presetSessId, presetAccount))

  "Kar171" should {
    "PUT /account/{{account_id}}/intent" in {
      val request = IntentUpdateRequest(presetAccount, session_id=presetSessId, IntentUnitRequest("why_00", "what_00", "when_00", "where_00")).toJson.toString

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
