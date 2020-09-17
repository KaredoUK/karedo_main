package core

import java.io.IOException

import akka.actor.{ActorLogging, Props, Actor}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import spray.client.pipelining._
import spray.http.Uri.Query

import spray.http._
import spray.json.DefaultJsonProtocol._ // marshallers
import spray.httpx.SprayJsonSupport._


import scala.concurrent.Future

object SMSActor {
  case class SendSMS(to: String, message: String, retryCount: Int = 3)

  def props(implicit bindingModule : BindingModule) = Props(classOf[SMSActor], bindingModule)

  def normaliseMsisdn(msisdn: String): String = {
   val internationalWithPlus = """\+(\d+)""".r
   val internationalWithDoubleZero = """00(\d+)""".r
   val genericNumeric = """(\d+)""".r

    msisdn match {
      case  internationalWithPlus(number) => number
      case internationalWithDoubleZero(number) => number
      case genericNumeric(number) => number
      case _ => throw new IllegalArgumentException(s"Invalid msisdn $msisdn")
    }
  }
}

class SMSActor(implicit val bindingModule : BindingModule) extends Actor with ActorLogging with Injectable {
  import SMSActor._

  //val user = injectProperty[String]("notification.sms.auth.user")
  //val pwd = injectProperty[String]("notification.sms.auth.pwd")
  val accessKey = injectProperty[String]("notification.sms.auth.accesskey")
  val serverEndpoint = injectProperty[String]("notification.sms.server.endpoint")
  val from = injectProperty[String]("notification.sms.sender")


  import context.dispatcher

  // val pipeline: SendReceive = sendReceive
  val pipeline: HttpRequest => Future[HttpResponse] = {
    addHeader("Authorization", s"AccessKey $accessKey") ~>
      sendReceive //~> unmarshal[String]
  }

  def receive: Receive = {
    case request@SendSMS(number, body, retryCount) => sendSMS(number, body) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendSMS(number, body, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }
  case class SMSRequest(recipients:String,originator:String,body:String)

  def sendSMS(to: String, body: String): Future[Unit] = {
    implicit val jsonSMSRequest = jsonFormat3(SMSRequest)
    if(accessKey==""){
      log.info(s"(((((Dummy sending sms to: $to, body: $body)))))")
      Future.successful()
    } else {
      log.debug(s"=====> Actual sending sms to $to")
      pipeline {
        Post(
          Uri(serverEndpoint),SMSRequest(normaliseMsisdn(to), from, body))

          /*
            query = Query(
              Map(
                "username" -> user,
                "password" -> pwd,
                "orig" -> from,
                "message" -> body,
                "to" -> normaliseMsisdn(to)

          )*/

      } map { httpResponse: HttpResponse =>
        if (httpResponse.status.isFailure) {
          throw new IOException(s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}")

        } else {
          log.info(s"Sent a sms, response from service is $httpResponse")
        }
      }
    }
  }
}
