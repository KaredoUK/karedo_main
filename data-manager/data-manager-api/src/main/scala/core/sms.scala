package core

import java.io.IOException

import akka.actor.{ ActorLogging, Props, Actor }
import com.escalatesoft.subcut.inject.{ Injectable, BindingModule }
import spray.client.pipelining._
import spray.http.Uri.Query

import spray.http._
import spray.json.DefaultJsonProtocol._ // marshallers
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Future

object SMSActor {
  case class SendSMS(to: String, message: String, retryCount: Int = 3)

  def props(implicit bindingModule: BindingModule) = Props(new SMSActor)

  def normaliseMsisdn(msisdn: String): String = {
    val internationalWithPlus = """\+(\d+)""".r
    val internationalWithDoubleZero = """00(\d+)""".r
    val genericNumeric = """(\d+)""".r

    msisdn match {
      case internationalWithPlus(number)       => number
      case internationalWithDoubleZero(number) => number
      case genericNumeric(number)              => number
      case _                                   => throw new IllegalArgumentException(s"Invalid msisdn $msisdn")
    }
  }
}

import SMSActor._

// used for tests
class DummySMSActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  log.info(s"[DUMMYSMS] [T:${Thread.currentThread().getId}]  instantiating DUMMY SMSActor")
  def receive: Receive = {
    case request @ SendSMS(number, body, retryCount) =>
      log.info(s"[DUMMYSMS] (sms to: $number, body: $body)")
  }
}
object DummySMSActor {
  def props(implicit bindingModule: BindingModule) = Props(new DummySMSActor)
}

// no need to specify anymore Textmarketer but in case we have the placeholder for additional services
class TextmarketerSMSActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {
  def receive: Receive = ???
}


class SMSActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {
  val accessKey = injectProperty[String]("notification.sms.auth.accesskey")
  val serverEndpoint = injectProperty[String]("notification.sms.server.endpoint")
  val from = injectProperty[String]("notification.sms.sender")

  log.info(s"[SMS] [T:${Thread.currentThread().getId}]  instantiating SMS with '$serverEndpoint' and accessKey '${accessKey}'")

  import context.dispatcher

  // val pipeline: SendReceive = sendReceive
  val pipeline: HttpRequest => Future[HttpResponse] = {
    addHeader("Authorization", s"AccessKey $accessKey") ~>
      sendReceive //~> unmarshal[String]
  }

  def receive: Receive = {
    case request @ SendSMS(number, body, retryCount) => sendSMS(number, body) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendSMS(number, body, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }
  case class SMSRequest(recipients: String, originator: String, body: String)

  def sendSMS(to: String, body: String): Future[Unit] = {
    implicit val jsonSMSRequest = jsonFormat3(SMSRequest)

    val msisdn: String = normaliseMsisdn(to)
    log.debug(s"[SMS] Sending sms serverEndpoint: $serverEndpoint from: $from to: $msisdn body: $body")
    pipeline {
      Post(
        Uri(serverEndpoint), SMSRequest(msisdn, from, body))
    } map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        throw new IOException(s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}")

      } else {
        log.error(s"[SMS] Sent a sms, response from service is $httpResponse")
      }
    }

  }
}
