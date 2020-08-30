package core

import java.net.URL
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.config.PropertiesConfigPropertySource
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import core.SMSActor.SendSMS
import org.apache.http.HttpStatus._
import org.apache.http.client.utils.URIBuilder
import org.specs2.matcher.ThrownExpectations
import org.specs2.mutable.SpecificationLike
import org.specs2.mutable.After
import org.specs2.specification.AllExpectations
import org.specs2.time.NoTimeConversions
import scala.collection.JavaConversions._
import util._
import scala.concurrent.duration._
import scala.util.Random

class SmsActorSpec extends TestKit(ActorSystem()) with SpecificationLike with Core
  with ImplicitSender with ThrownExpectations with NoTimeConversions {



  trait WithWireMockServer extends After {
    lazy val mockServiceListeningPort = 30000 + Random.nextInt(3000)

    implicit val bindingModule = newBindingModuleWithConfig( PropertiesConfigPropertySource(
        Map(
          "notification.sms.auth.user" -> "user",
          "notification.sms.auth.pwd" -> "pwd",
          "notification.sms.server.endpoint" -> s"http://localhost:$mockServiceListeningPort/smsEndpoint",
          "notification.sms.sender" -> "sender"
        )
      )
    )

    var smsActor = system.actorOf(SMSActor.props)

    var wireMockServer : WireMockServer = null
    wireMockServer = new WireMockServer(wireMockConfig().port(mockServiceListeningPort));
    wireMockServer.start();

    WireMock.configureFor("localhost", mockServiceListeningPort);


    def after =
      if(wireMockServer != null)
        wireMockServer.stop()
  }


  sequential

  "SMS actor should" >> {

    "Request SMS delivery to textmarketer.co.uk" in new WithWireMockServer {
      givenThat {
        get(urlMatching("/smsEndpoint.*")) willReturn (aResponse withStatus (SC_OK))
      }

      smsActor ! SendSMS("4412345", "message")


      withinTimeout(1.minutes) {

        val loggedRequests = findAll(getRequestedFor(urlMatching("/smsEndpoint.*")))

        loggedRequests should haveSize(1)

        val parameters = extractQueryParams(loggedRequests.head.getUrl)

        parameters shouldEqual Map(
          "username" -> "user",
          "password" -> "pwd",
          "orig" -> "sender",
          "message" -> "message",
          "to" -> "4412345"
        )
      }
    }
  }


  def extractQueryParams(url: String): Map[String, String] =
    new URIBuilder(url).getQueryParams.map { nameValuePair => nameValuePair.getName -> nameValuePair.getValue}.toMap

}
