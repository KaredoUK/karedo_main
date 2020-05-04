package core

import akka.actor.{ActorLogging, Props, Actor}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import spray.client.pipelining._
import spray.http.BasicHttpCredentials

import scala.concurrent.Future

object SMSActor {
  case class SendSMS(to: String, message: String, retryCount: Int = 3)

  def props(implicit bindingModule : BindingModule) = Props(classOf[SMSActor], bindingModule)
}

class SMSActor(implicit val bindingModule : BindingModule) extends Actor with ActorLogging with Injectable {
  import SMSActor._

  val user = injectProperty[String]("notification.sms.auth.user")
  val pwd = injectProperty[String]("notification.sms.auth.pwd")
  val serverEndpoint = injectProperty[String]("notification.email.server.endpoint")
  val from = injectProperty[String]("notification.email.sender")

  import context.dispatcher

  val requestPipeline = addCredentials(BasicHttpCredentials(user, pwd)) ~> sendReceive

  def receive: Receive = {
    case request@SendSMS(number, body, retryCount) => sendSMS(number, body) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendSMS(number, body, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }

  def sendSMS(to: String, body: String): Future[Unit] = ???
}
