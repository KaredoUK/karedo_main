package specs4_login

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

/**
  * Created by pakkio on 10/20/16.
  */
trait Kar141_SendCode_Test {
  self: Specs4_LoginSequence =>

  "Kar141_SendCode_test" should {


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
