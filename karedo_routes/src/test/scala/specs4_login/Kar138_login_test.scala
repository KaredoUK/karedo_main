package specs4_login

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

/**
  * Created by pakkio on 10/21/16.
  */
trait Kar138_login_test {
  self : Specs4_LoginSequence =>

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
}
