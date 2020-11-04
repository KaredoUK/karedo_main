package api

import java.util.UUID

import akka.actor.ActorRefFactory
import com.parallelai.wallet.datamanager.data.{APISessionResponse, BrandResponse, ApiDataJsonProtocol}
import org.specs2.specification.Fragments
import org.specs2.mutable._
import spray.client.pipelining._
import spray.http.{HttpResponse, HttpRequest}
import util.ApiHttpClientSpec

import scala.concurrent.Future

//import util.ApiHttpClientSpec.WithMockedPersistenceRestService

/**
 * Created by user on 12/24/14.
 */
class AuthenticationSessionIDSpec
  extends ApiHttpClientSpec
{
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._


  "POST /login" should {
    "1) give 400 with invalid json" >> new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val data = Map(
        //"user" -> "user",
        "password1" -> "password"
      )

      val response = wait(pipeline {
        Post(s"$serviceUrl/login", data)
      })

      response.status.intValue should be_===(400)
    }


    "2) give 200 with correct json" >> new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val data = Map(
        //"user" -> "user",
        "password" -> "password"
      )

      val response = wait(pipeline {
        Post(s"$serviceUrl/login", data)
      })

      response.status.intValue should be_===(200)
    }

    "3) return a valid UUID as sessionId" >> new WithMockedPersistenceRestService {

      val p = sendReceive ~> unmarshal[APISessionResponse]

      val data = Map(
        //"user" -> "user",
        "password" -> "password"
      )

      val response=wait(p {
        Post(s"$serviceUrl/login", data)
      })

      val rebuiltUUID=UUID.fromString(response.sessionId).toString
      response.sessionId must beEqualTo(rebuiltUUID)
    }

  }
  "After authentication" should {
    "4) give 403 unauthorized if not login or not header" >> new WithMockedPersistenceRestService {
      val sessionId=UUID.randomUUID().toString
      val pipeline = addHeader("session",sessionId) ~> sendReceive


      val response = wait(pipeline {
        Get(s"$serviceUrl/testAuth")
      })
      response.status.intValue should be_===(403)
    }

    "5) give 200 if session ok" >> new WithMockedPersistenceRestService {

      val p = sendReceive ~> unmarshal[APISessionResponse]
      val data = Map(
        //"user" -> "user",
        "password" -> "password"
      )


      val r = wait(p {
        Post(s"$serviceUrl/login", data)
      })


      val sessionId=r.sessionId

      val pipeline = addHeader("session",sessionId) ~> sendReceive


      val response = wait(pipeline {
        Get(s"$serviceUrl/testAuth")
      })
      response.status.intValue should be_===(200)
    }

    "6) give 403 if session timeout" >> new WithMockedPersistenceRestService {

      val p = sendReceive ~> unmarshal[APISessionResponse]
      val data = Map(
        //"user" -> "user",
        "password" -> "password"
      )


      val r = wait(p {
        Post(s"$serviceUrl/login", data)
      })


      val sessionId=r.sessionId

      Thread.sleep(4000)

      val pipeline = addHeader("session",sessionId) ~> sendReceive


      val response = wait(pipeline {
        Get(s"$serviceUrl/testAuth")
      })
      response.status.intValue should be_===(403)
    }

  }






}
