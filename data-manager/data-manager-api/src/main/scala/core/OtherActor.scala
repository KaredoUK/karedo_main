package core

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.parallelai.wallet.datamanager.data._

import core.OtherActor.{InvalidOtherRequest, InternalOtherError, OtherError}
import org.joda.time.DateTime

import spray.json._

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * We use the companion object to hold all the messages that the ``OfferActor``
 * receives.
 */
object OtherActor {

  def props()(implicit bindingModule: BindingModule): Props =
    Props(classOf[OtherActor], bindingModule)


  sealed trait OtherError

  case class InvalidOtherRequest(reason: String) extends OtherError
  case class InternalOtherError(reason: Throwable) extends OtherError

  implicit object otherErrorJsonFormat extends RootJsonWriter[OtherError] {
    def write(error: OtherError) = error match {
      case InvalidOtherRequest(reason) => JsObject(
        "type" -> JsString("OtherInvalidRequest"),
        "data" -> JsObject {
          "reason" -> JsString(reason)
        }
      )
      case InternalOtherError(reason) => JsObject(
        "type" -> JsString("InternalOtherError"),
        "data" -> JsObject {
          "reason" -> JsString(reason.getMessage)
        }
      )
    }
  }
}

class OtherActor()(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  def receive: Receive = {
    case _ => sender ! "" // case request: OfferData => replyToSender(createOffer(request))
  }


  def replyToSender[T <: Any](response: Future[ResponseWithFailure[OtherError, T]]): Unit = {
    val replyTo = sender

    response recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InternalOtherError(t))
    } foreach {
      responseContent : ResponseWithFailure[OtherError, T] =>
        replyTo ! responseContent
    }
  }

}
