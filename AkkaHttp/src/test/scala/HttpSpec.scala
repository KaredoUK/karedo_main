import java.util.concurrent.TimeUnit

import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import dispatch.{Http, url}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class HttpSpec extends Specification
  with WebService
  with Matchers {


  sequential
  "A web server" should {
    "* work with standard gets" in  {

      val request = url(s"http://127.0.0.1:8080/question").GET
      val responseFuture = Http(request)

      val response = Await.result(responseFuture, Duration(500, TimeUnit.MILLISECONDS))
      response.getResponseBody must beEqualTo("answer")


    }
    "* work with post" in {
      val sent = """{"request":"hello"}"""
      val request = url(s"http://127.0.0.1:8080/post")
        .POST
        .setContentType("application/json","UTF8")
        .setBody(sent)

      val responseFuture = Http(request)

      val response = Await.result(responseFuture, Duration(500, TimeUnit.MILLISECONDS))
      response.getResponseBody must beEqualTo(s"answer to $sent")

    }
  }
}
