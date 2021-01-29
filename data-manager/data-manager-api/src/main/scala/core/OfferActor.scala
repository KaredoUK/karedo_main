package core

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.parallelai.wallet.datamanager.data._

import core.OfferActor.{InvalidOfferRequest, InternalOfferError, OfferError}
import org.joda.time.DateTime
import parallelai.wallet.entity.{Brand, _}
import parallelai.wallet.persistence.{OfferDAO, BrandDAO}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * We use the companion object to hold all the messages that the ``OfferActor``
 * receives.
 */
object OfferActor {

  /*def props(offerDAO: OfferDAO)(implicit bindingModule: BindingModule): Props =
    Props(classOf[OfferActor], offerDAO, bindingModule)*/


  sealed trait OfferError

  case class InvalidOfferRequest(reason: String) extends OfferError
  case class InternalOfferError(reason: Throwable) extends OfferError

  implicit object offerErrorJsonFormat extends RootJsonWriter[OfferError] {
    def write(error: OfferError) = error match {
      case InvalidOfferRequest(reason) => JsObject(
        "type" -> JsString("OfferInvalidRequest"),
        "data" -> JsObject {
          "reason" -> JsString(reason)
        }
      )
      case InternalOfferError(reason) => JsObject(
        "type" -> JsString("InternalOfferError"),
        "data" -> JsObject {
          "reason" -> JsString(reason.getMessage)
        }
      )
    }
  }
}

class OfferActor(implicit offerDAO: OfferDAO, implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  def receive: Receive = {
    case request: OfferData => replyToSender(createOffer(request))
  }

  def createOffer(request: OfferData): Future[ResponseWithFailure[OfferError, OfferResponse]] = successful {

    validateOffer(request) match {
      case None =>
        log.info("Creating new offer for request {}", request)
        val newoffer = Offer(name=request.name, brandId=request.brandId, description=request.desc,
          imagePath=request.imagePath, qrCodeId=request.qrCodeId, value=request.value)
        val uuid=offerDAO.insertNew(newoffer).get
        val response=OfferResponse(uuid)
        SuccessResponse(response)

      case Some(error) =>
        log.info("Validation failed "+error)
        FailureResponse(InvalidOfferRequest(error))
    }
  }

  def validateOffer(request: OfferData): Option[String] = request match {
    case OfferData("", _, _, _, _, _) => Some("Name must be not empty")
    case _ => None
  }

  def replyToSender[T <: Any](response: Future[ResponseWithFailure[OfferError, T]]): Unit = {
    val replyTo = sender

    response recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InternalOfferError(t))
    } foreach {
      responseContent : ResponseWithFailure[OfferError, T] =>
        replyTo ! responseContent
    }
  }

}
