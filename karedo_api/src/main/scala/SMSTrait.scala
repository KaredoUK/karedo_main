import java.io.IOException

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, Materializer}
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

trait SMSTrait extends SprayJsonSupport with DefaultJsonProtocol {
  val accessKey: String
  val serverEndpoint: String
  val from: String
  implicit val system: ActorSystem
  implicit val materializer: Materializer
  val logger = LoggerFactory.getLogger(classOf[SMSTrait])

  case class SMSRequest(recipients: String, originator: String, body: String)

  def sendSMS(msisdn: String, body: String)(implicit actorRefFactory: ActorRefFactory): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val jsonSMSRequest = jsonFormat3(SMSRequest)

    //val msisdn: String = normaliseMsisdn(to)
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
object testSMS extends App {
  implicit val system = ActorSystem("demo")
  val it = new SMSTrait {
    override val from: String = "Karedo"
    override val serverEndpoint: String = "https://rest.messagebird.com/messages"
    override val accessKey: String = "live_RmtS0fUQafb7ae0XK21PVmIpt"
    override implicit val system = ActorSystem("testSMS")
    override val materializer = ActorMaterializer()
  }

  it.sendSMS("00393319345235","text of message 2")
}


