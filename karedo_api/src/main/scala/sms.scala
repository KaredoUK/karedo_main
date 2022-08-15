import java.io.IOException

import akka.actor.{Actor, ActorLogging, ActorRefFactory, ActorSystem, Props}
import akka.http.javadsl.model.headers.CustomHeader
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.{Http, model}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, Uri}
import karedo.entity.dao.Configurable
import org.slf4j.LoggerFactory
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.RawHeader
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import spray.json.DefaultJsonProtocol


object SMSActor {
  case class SendSMS(to: String, message: String, retryCount: Int = 3)

  def props = Props(new SMSActor)

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
class DummySMSActor extends Configurable
  with Actor
  with ActorLogging {

  log.info(s"[DUMMYSMS] [T:${Thread.currentThread().getId}]  instantiating DUMMY SMSActor")
  def receive: Receive = {
    case request @ SendSMS(number, body, retryCount) =>
      log.info(s"[DUMMYSMS] (sms to: $number, body: $body)")
  }
}
object DummySMSActor {
  def props = Props(new DummySMSActor)
}

// no need to specify anymore Textmarketer but in case we have the placeholder for additional services
class TextmarketerSMSActor extends Configurable with Actor with ActorLogging {
  def receive: Receive = ???
}

trait SMSTrait extends SprayJsonSupport with DefaultJsonProtocol {
  val accessKey: String
  val serverEndpoint: String
  val from: String
  implicit val system: ActorSystem
  implicit val materializer: Materializer
  val logger = LoggerFactory.getLogger(classOf[SMSTrait])

  case class SMSRequest(recipients: String, originator: String, body: String)

  def sendSMS(to: String, body: String)(implicit actorRefFactory: ActorRefFactory): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val jsonSMSRequest = jsonFormat3(SMSRequest)

    val msisdn: String = normaliseMsisdn(to)
    logger.info(s"[SMS] Sending sms serverEndpoint: $serverEndpoint from: $from to: $msisdn body: $body")

    Http().singleRequest(HttpRequest(POST,Uri(serverEndpoint),entity=SMSRequest(msisdn, from, body).toJson.toString).
      addHeader(RawHeader("Authorization", s"AccessKey $accessKey"))).map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        val err = s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}"
        logger.error(err)
        throw new IOException(err)

      } else {
        logger.info(s"[SMS] Sent a sms, response from service is $httpResponse")
      }
    }

  }

}
/*object testSMS extends App {
  val it = new SMSTrait {
    override val from: String = "Karedo"
    override val serverEndpoint: String = "https://rest.messagebird.com/messages"
    override val accessKey: String = "live_RmtS0fUQafb7ae0XK21PVmIpt"
  }
  import akka.actor.ActorSystem

  implicit val system = ActorSystem("demo")
  it.sendSMS("00393319345235","text of message")
}*/

class SMSActor
  extends Configurable
    with Actor
    with SMSTrait
    with ActorLogging {
  override val accessKey = conf.getString("notification.sms.auth.accesskey")
  override val serverEndpoint = conf.getString("notification.sms.server.endpoint")
  override val from = conf.getString("notification.sms.sender")


  log.info(s"[SMS] [T:${Thread.currentThread().getId}]  instantiating SMS with '$serverEndpoint' and accessKey '${accessKey}'")

  import context.dispatcher



  def receive: Receive = {
    case request @ SendSMS(number, body, retryCount) => sendSMS(number, body) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendSMS(number, body, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }

}
