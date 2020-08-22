package core

import akka.actor.{Actor, ActorLogging, Props}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.parallelai.wallet.datamanager.data._
import core.BrandActor.{BrandError, InternalBrandError, InvalidBrandRequest}
import parallelai.wallet.entity.{Brand, _}
import parallelai.wallet.persistence.BrandDAO
import spray.json._

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * We use the companion object to hold all the messages that the ``BrandActor``
 * receives.
 */
object BrandActor {

  def props(brandDAO: BrandDAO)(implicit bindingModule: BindingModule): Props =
    Props(classOf[BrandActor], brandDAO, bindingModule)


  sealed trait BrandError
  case class InvalidBrandRequest(reason: String) extends BrandError
  case class InternalBrandError(reason: Throwable) extends BrandError

  implicit object brandErrorJsonFormat extends RootJsonFormat[BrandError] {
    def write(error: BrandError) = error match {
      case InvalidBrandRequest(reason) => JsObject(
        "type" -> JsString("BrandInvalidRequest"),
        "data" -> JsObject {
          "reason" -> JsString(reason)
        }
      )
      case InternalBrandError(reason) => JsObject(
        "type" -> JsString("InternalBrandError"),
        "data" -> JsObject {
          "reason" -> JsString(reason.getMessage)
        }
      )
    }

    def read(value: JsValue) = {
      def readReasonFromData(attributes: Map[String, JsValue]): String = {
        val data = attributes.get("data") map { _.asInstanceOf[JsObject] }
        data match {
          case None =>
            "Unknown reason"

          case Some(errorData) =>
            errorData.fields.get("reason") map { _.toString } getOrElse "Unknown reason"
        }
      }


      value match {
        case JsObject(attributes) =>
          attributes.get("type") match {
            case Some(JsString("BrandInvalidRequest")) =>
              InvalidBrandRequest(readReasonFromData(attributes))

            case Some(JsString("InternalBrandError")) =>
              InternalBrandError(new Throwable(readReasonFromData(attributes)))

            case _ => throw new IllegalArgumentException(s"Unable to unmarshall BrandError from JSON value ${value.prettyPrint}, invalid type field value")
          }

        case _ => throw new IllegalArgumentException(s"Unable to unmarshal BrandError from JSON value ${value.prettyPrint}")
      }
    }
  }

}

class BrandActor(brandDAO: BrandDAO)(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  def receive: Receive = {
    case request: BrandData => replyToSender(createBrand(request))
    case ListBrands => sender ! listBrands
  }

  def replyToSender[T <: Any](response: Future[ResponseWithFailure[BrandError, T]]): Unit = {
    val replyTo = sender

    response recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InternalBrandError(t))
    } foreach {
      responseContent : ResponseWithFailure[BrandError, T] =>
        replyTo ! responseContent
    }
  }

  def createBrand(request: BrandData): Future[ResponseWithFailure[BrandError, BrandResponse]] = successful {

    validateBrand(request) match {
      case None =>
        log.info("Creating new brand for request {}", request)
        val newbrand = Brand(name = request.name, iconPath = request.iconPath, ads = List[AdvertisementMetadata]())
        val uuid=brandDAO.insertNew(newbrand).get
        val response=BrandResponse(uuid)
        SuccessResponse(response)

      case Some(error) =>
        log.info("Validation failed "+error)
        FailureResponse(InvalidBrandRequest(error))
    }
  }
  def listBrands: List[BrandRecord]= {

    brandDAO.list.map(b => BrandRecord(b.id,b.name, b.iconPath))

  }

  def validateBrand(request: BrandData): Option[String] = request match {
    case BrandData("", _) => Some("Name must be not empty")
    case BrandData(_, "") => Some("IconPath must be not empty")
    case _ => None
  }
}
