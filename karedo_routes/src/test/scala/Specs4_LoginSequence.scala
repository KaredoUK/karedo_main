import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.entity._
import karedo.entity.dao.MongoConnection
import karedo.routes.Routes
import karedo.util.{KaredoJsonHelpers, _}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.{span, _}

/**
  * Created by pakkio on 05/10/16.
  */
class Specs4_LoginSequence extends WordSpec
  with MongoConnection
  with Routes
  with ScalatestRouteTest
  with DbCollections
  with KaredoJsonHelpers
  with KaredoConstants

  with Matchers {

  implicit val timeout = RouteTestTimeout(1000.second(span))

  // clear everything for tests to be understandable
  mongoClient.dropDatabase(mongoDbName)

  val acctId = getNewRandomID
  val appId = getNewRandomID

  dbUserApp.insertNew(UserApp(appId,acctId))
  dbUserAccount.insertNew(UserAccount(acctId,password=Some("pippo")))

  var sessionId: String = ""


  "Kar138_login" should {


    "* POST /account/{{account_id}}/application/{{application_id}}/login" in {
      val request = Kar138Req(password="pippo").toJson.toString
      Post(s"/account/$acctId/application/$appId/login",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[Kar138Res]
          sessionId = res.session_id
          status.intValue() shouldEqual (200)
        }

    }
  }
  "Kar141_SendCode" should {


    "* POST /account" in {
      val request = Kar141_SendCode_Req(
        application_id = appId,
        first_name = "John",
        last_name = "Doe",
        msisdn = "0123456",
        user_type = "",
        email = "john@doe.com"

      ).toJson.toString
      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[Kar141_SendCode_Res]
          status.intValue() shouldEqual (200)
        }

    }
  }

}
