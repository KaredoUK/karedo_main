import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}


class WebSpec extends WordSpec
  with ScalatestRouteTest
  with Entities
  with Matchers {


  "The webservice" should {

    val routes = Routes.route

    "* work with standard gets" in {
      Get("/get/question") ~> routes ~> check {
        responseAs[String] shouldEqual "<h1>Hello get question</h1>"
      }


    }
    "* work with post" in {
      val sent = """{"request":"hello"}"""
      Post("/post", HttpEntity(ContentTypes.`application/json`, sent)) ~>
        routes ~> check {
        responseAs[String] shouldEqual s"<h1>Hello post $sent</h1>"
      }
    }
    "* work with json" in {
      val record = Record(5, "hello")
      Post("/json", record) ~>
        routes ~> check {
        responseAs[Record] shouldEqual Record(record.a+1,record.b+"!")
      }
    }
  }

}
