package core

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import org.specs2.mutable.{ BeforeAfter, Specification }
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import dispatch.{ Http, url }


object StubServer extends Specification {

  val Port = 8080
  val Host = "localhost"

  trait StubServer extends BeforeAfter {
    val wireMockServer = new WireMockServer(wireMockConfig().port(Port))

    def before = {
      wireMockServer.start()
      WireMock.configureFor(Host, Port)
    }

    def after = wireMockServer.stop()
  }

  sequential
  "WireMock" should {
    "stub get request" in new StubServer {
      val path = "/my/resource"
      stubFor(get(urlEqualTo(path))
        .willReturn(
          aResponse()
            .withStatus(200)
          .withBody("get called")))

      val request = url(s"http://$Host:$Port$path").GET
      val responseFuture = Http(request)

      val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
      response.getStatusCode mustEqual 200
      response.getResponseBody mustEqual "get called"
    }
  }
  "stub post request" in new StubServer {
    val path = "/my/resource"
    stubFor(post(urlEqualTo(path))
      .willReturn(
        aResponse()
          .withStatus(200)
        .withBody("post called")))

    val request = url(s"http://$Host:$Port$path")
      .POST
      .setBody("hello")
      .setContentType("application/json","UTF8")

    val responseFuture = Http(request)

    val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
    response.getStatusCode mustEqual 200
    response.getResponseBody mustEqual "post called"
  }
}

