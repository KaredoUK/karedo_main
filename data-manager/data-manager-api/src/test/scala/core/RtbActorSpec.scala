package core


import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.config.PropertiesConfigPropertySource
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{RequestPatternBuilder, UrlMatchingStrategy, WireMock}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.http.RequestMethod._
import core.RtbActor.SendRtb
import org.specs2.matcher.ThrownExpectations
import org.specs2.mutable.{After, SpecificationLike}
import org.specs2.time.NoTimeConversions
import util._

import scala.concurrent.duration._
import scala.util.Random


class RtbActorSpec
  extends TestKit(ActorSystem())
    with SpecificationLike
    with Core
    with ImplicitSender
    with ThrownExpectations
    with NoTimeConversions {

  trait WithWireMockServer extends After {
    lazy val mockServiceListeningPort = 30000 + Random.nextInt(3000)

    implicit val bindingModule = newBindingModuleWithConfig(PropertiesConfigPropertySource(
      Map(
        "notification.rtb.server.endpoint" ->
          s"http://localhost:$mockServiceListeningPort/bids"
      )
    )
    )

    var rtbActor = system.actorOf(RtbActor.props)

    val wireMockServer = new WireMockServer(wireMockConfig().port(mockServiceListeningPort))
    wireMockServer.start()

    WireMock.configureFor("localhost", mockServiceListeningPort)


    def after = wireMockServer.stop()
  }


  sequential

  "Rtb actor should" >> {

    "accept http request with json " in new WithWireMockServer {
      val path: String = "/bids"

      stubFor(post(urlEqualTo(path))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody("post called")))


      rtbActor ! SendRtb("aaa", "bbb", 1)


      withinTimeout(5.seconds) {
        val url: UrlMatchingStrategy = urlEqualTo(path)
        verify(1, postRequestedFor(url))
        val strategy = new UrlMatchingStrategy()
        strategy.setUrl(path)
        val requests = findAll(new RequestPatternBuilder(POST, strategy))

        requests.size must beEqualTo(1)
        requests.get(0).getBodyAsString must beEqualTo(
          """{
            |  "recipients": "aaa",
            |  "originator": "aaa",
            |  "body": "bbb"
            |}""".stripMargin)

      }
    }
  }


}
