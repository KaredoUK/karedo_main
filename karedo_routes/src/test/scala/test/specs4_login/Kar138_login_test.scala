package test.specs4_login

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.entity.{UserAccount, UserApp}
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.scalatest.junit.JUnitRunner

/**
  * Created by pakkio on 10/21/16.
  */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar138_login_test extends AllTests {

  val acctId = getNewRandomID
  val appId = getNewRandomID

  dbUserApp.insertNew(UserApp(appId,acctId))
  dbUserAccount.insertNew(UserAccount(acctId,password=Some("pippo")))


  "Kar138_login" should {


    "* POST /account/{{account_id}}/application/{{application_id}}/login" in {
      val request = post_LoginRequest(acctId, appId, password="pippo").toJson.toString
      Post(s"/login",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SessionIdResponse]
          val sessionId = res.session_id
          status.intValue() shouldEqual (200)
        }

    }
  }
}
