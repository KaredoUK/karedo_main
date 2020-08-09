package core

import java.io.IOException

import akka.actor.{ActorLogging, Props, Actor}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import spray.client.pipelining._
import spray.http.Uri.Query
import spray.http.{Uri, HttpResponse, FormData, BasicHttpCredentials}

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

  val user = injectProperty[String]("notification.sms.auth.user")
  val pwd = injectProperty[String]("notification.sms.auth.pwd")
  val serverEndpoint = injectProperty[String]("notification.sms.server.endpoint")
  val from = injectProperty[String]("notification.sms.sender")

  import context.dispatcher

  val pipeline: SendReceive = sendReceive

  def receive: Receive = {
    case request@SendSMS(number, body, retryCount) => sendSMS(number, body) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendSMS(number, body, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }

  def sendSMS(to: String, body: String): Future[Unit] = {
    if(user==""){
      Future(println(s"Dummy sending sms to: $to, body: $body"))
    } else
    pipeline {
      Get(
        Uri(serverEndpoint).copy(
          query = Query(
              Map(
                "username" -> user,
                "password" -> pwd,
                "orig" -> from,
                "message" -> body,
                "to" -> normaliseMsisdn(to)
              )
            )
          )
      )
    } map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        throw new IOException(s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}")
      } else {
        ()
      }
    }
  }
}
