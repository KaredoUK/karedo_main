package core

import java.net.URI

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import core.BrandActor.{InvalidBrandRequest, BrandError}
import core.MessengerActor.SendMessage
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import akka.actor.{Props, ActorLogging, ActorRef, Actor}
import com.parallelai.wallet.datamanager.data._
import sun.management.jdp.JdpJmxPacket
import scala.concurrent.{ExecutionContext, Future}
import scala.async.Async._
import java.util.UUID
import spray.json._
import parallelai.wallet.entity._
import com.parallelai.wallet.datamanager.data._
import scala.Some
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang.RandomStringUtils
import javax.management.InvalidApplicationException
import scala.Some
import scala.Some
import scala.concurrent.Future.successful

/**
 * We use the companion object to hold all the messages that the ``BrandActor``
 * receives.
 */
object BrandActor {

  def props(brandDAO: BrandDAO)(implicit bindingModule: BindingModule): Props =
    Props(classOf[BrandActor], brandDAO, bindingModule)


  sealed trait BrandError

  case class InvalidBrandRequest(reason: String) extends BrandError
  case class InternalBrandError(reason: String) extends BrandError

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
          "reason" -> JsString(reason)
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
              InternalBrandError(readReasonFromData(attributes))

            case _ => throw new IllegalArgumentException(s"Unable to unmarshall BrandError from JSON value ${value.prettyPrint}, invalid type field value")
          }

        case _ => throw new IllegalArgumentException(s"Unable to unmarshal BrandError from JSON value ${value.prettyPrint}")
      }
    }
  }

}

class BrandActor(brandDAO: BrandDAO)(implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  def receive: Receive = {
    case request: BrandData => sender ! createBrand(request)
  }

  def createBrand(request: BrandData): Either[BrandError, BrandResponse] = {

    validateBrand(request) match {
      case None =>
        log.info("Creating new brand for request {}", request)
        val newbrand = Brand(name = request.name, iconPath = request.iconPath, ads = List[AdvertisementMetadata]())
        val uuid=brandDAO.insertNew(newbrand).get
        val response=BrandResponse(uuid)
        Right(response)

      case Some(error) =>
        log.info("Validation failed "+error)
        Left(InvalidBrandRequest(error))
    }
  }

  def validateBrand(request: BrandData): Option[String] = request match {
    case BrandData("", _) => Some("Name must be not empty")
    case BrandData(_, "") => Some("IconPath must be not empty")
    case _ => None
  }
}
