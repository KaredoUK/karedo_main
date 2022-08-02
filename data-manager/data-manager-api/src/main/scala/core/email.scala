package core

import java.io.IOException

import akka.actor.{Actor, ActorLogging, ActorRefFactory, Props}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import core.EmailActor.SendEmail
import org.slf4j.LoggerFactory
import spray.client.pipelining._
import spray.http.{BasicHttpCredentials, FormData, HttpResponse}

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
      log.info(s"[DUMMYEMAIL] [T:${Thread.currentThread().getId}] (Email should be sent to $to, subject: $subject, body: $body)")
  }
}

object DummyEmailActor {
  def props(implicit bindingModule: BindingModule) = Props(classOf[DummyEmailActor], bindingModule)
}

trait EmailTrait {
  val userKey: String
  val prefix: String
  val serverEndpoint: String
  val from : String
  val logger = LoggerFactory.getLogger(classOf[EmailTrait])


  def sendEmail(to: String, body: String, subject: String)(implicit actorRefFactory: ActorRefFactory): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val requestPipeline = addCredentials(BasicHttpCredentials("api", s"$prefix$userKey")) ~> sendReceive

    logger.info(s"[EMAIL] Sending email endpoint: $serverEndpoint from $from to $to subject $subject")
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
        logger.info(s"[EMAIL] Got an error response is ${httpResponse.entity.asString}")
        throw new IOException(s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}")
      } else {
        ()
      }
    }
  }
}

class EmailActor(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable with EmailTrait {

  override val userKey = injectProperty[String]("notification.email.auth.user.key")
  override val prefix = injectProperty[String]("notification.email.auth.user.prefix")
  override val serverEndpoint = injectProperty[String]("notification.email.server.endpoint")
  override val from = injectProperty[String]("notification.email.sender")
  log.info(s"[T:${Thread.currentThread().getId}]  instantiating email with '$serverEndpoint'")

  import context.dispatcher


  def receive: Receive = {

    case request@SendEmail(to, body, subject, retryCount) => sendEmail(to, body, subject) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendEmail(to, body, subject, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }



}
