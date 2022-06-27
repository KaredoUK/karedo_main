import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future


class AkkaHttpTestkitSpec extends WordSpec
  with ScalatestRouteTest
  with Matchers {


 "The webservice" should {

   val routes = WebService.questionRoutes

   "* work with standard gets" in {
     Get("/question") ~> routes ~> check {
       responseAs[String] shouldEqual "answer"
     }


   }
   "* work with post" in {
     val sent = """{"request":"hello"}"""
     Post("/post", HttpEntity(ContentTypes.`application/json`, sent)) ~>
       routes ~> check {
       responseAs[String] shouldEqual s"answer to $sent"
     }
   }
 }

}
