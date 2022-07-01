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

object RtbActor {
  case class SendRtb(to: String, message: String, retryCount: Int = 3)

  def props(implicit bindingModule: BindingModule) = Props(new RtbActor)

}

import RtbActor._

// used for tests
class DummyRtbActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  log.info(s"[T:${Thread.currentThread().getId}]  instantiating DUMMY SMSActor")
  def receive: Receive = {
    case request @ SendRtb(number, body, retryCount) =>
      log.info(s"(((((Dummy sending rtb to: $number, body: $body)))))")
  }
}
object DummyRtbActor {
  def props(implicit bindingModule: BindingModule) = Props(new DummyRtbActor)
}


class RtbActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {
  val serverEndpoint = injectProperty[String]("notification.rtb.server.endpoint")

  log.info(s"[T:${Thread.currentThread().getId}]  instantiating Rtb with '$serverEndpoint'")

  import context.dispatcher

  // val pipeline: SendReceive = sendReceive
  val pipeline: HttpRequest => Future[HttpResponse] = {
    //addHeader("Authorization", s"AccessKey $accessKey") ~>
      sendReceive //~> unmarshal[String]
  }

  def receive: Receive = {
    case request @ SendRtb(number, body, retryCount) => sendRtb(number, body) recover {
      case exception if retryCount > 0 =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendRtb(number, body, retryCount - 1)
      case exception =>
        log.error("Unable to send rtb message {}, exception {}", request, exception)
    }
  }
  case class RtbRequest(recipients: String, originator: String, body: String)

  def sendRtb(to: String, body: String): Future[Unit] = {
    implicit val jsonSMSRequest = jsonFormat3(RtbRequest)

    log.debug(s"=====> Actual sending rtb to $to")
    pipeline {
      Post(
        Uri(serverEndpoint), RtbRequest(to, to, body))
    } map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        throw new IOException(s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}")

      } else {
        log.info(s"Sent a rtb, response from service is $httpResponse")
      }
    }

  }
}
