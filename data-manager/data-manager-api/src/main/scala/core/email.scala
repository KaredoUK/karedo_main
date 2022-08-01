package core

import java.io.IOException

import akka.actor.{Props, ActorLogging, ActorRefFactory, Actor}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import core.EmailActor.SendEmail
import spray.client.pipelining._
import spray.http.{HttpResponse, FormData, BasicHttpCredentials}

import scala.concurrent.Future

object EmailActor {

  case class SendEmail(to: String, message: String, subject: String, retryCount: Int = 3)

  def props(implicit bindingModule: BindingModule) = Props(classOf[EmailActor], bindingModule)
}

class DummyEmailActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging {

  import context.dispatcher
  log.info(s"[T:${Thread.currentThread().getId}] Instantiating Dummy Email Actor")

  def receive: Receive = {
    case request@SendEmail(to, body, subject, retryCount) =>
      log.debug(s"[T:${Thread.currentThread().getId}] (((((Dummy Email not sent to $to, subject: $subject, body: $body)))))")
  }
}

object DummyEmailActor {
  def props(implicit bindingModule: BindingModule) = Props(classOf[DummyEmailActor], bindingModule)
}

class EmailActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  val userKey = injectProperty[String]("notification.email.auth.user.key")
  val prefix = injectProperty[String]("notification.email.auth.user.prefix") orElse "key-"
  val serverEndpoint = injectProperty[String]("notification.email.server.endpoint")
  val from = injectProperty[String]("notification.email.sender")
  log.info(s"[T:${Thread.currentThread().getId}]  instantiating email with '$serverEndpoint'")

  import context.dispatcher

  val requestPipeline = addCredentials(BasicHttpCredentials("api", s"$prefix$userKey")) ~> sendReceive

  def receive: Receive = {

    case request@SendEmail(to, body, subject, retryCount) => sendEmail(to, body, subject) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendEmail(to, body, subject, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }

  def sendEmail(to: String, body: String, subject: String)(implicit actorRefFactory: ActorRefFactory): Future[Unit] = {

    log.debug(s"======> actual sending email to $to")
    requestPipeline {
      Post(
        serverEndpoint,
        FormData(
          Map(
            "from" -> from,
            "to" -> to,
            "subject" -> subject,
            "text" -> body
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
