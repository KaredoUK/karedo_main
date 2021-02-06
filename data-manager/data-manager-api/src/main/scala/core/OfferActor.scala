package core

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.parallelai.wallet.datamanager.data._

import core.OfferActor.{InvalidOfferRequest, InternalOfferError, OfferError}
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime
import parallelai.wallet.entity.{Brand, _}
import parallelai.wallet.persistence.{UserAccountDAO, KaredoSalesDAO, OfferDAO, BrandDAO}
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

class OfferActor(implicit val saleDAO: KaredoSalesDAO,
                 implicit val offerDAO: OfferDAO,
                 implicit val brandDAO: BrandDAO,
                 implicit val customerDAO: UserAccountDAO,
                 implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {

  def receive: Receive = {
    case request: OfferData => replyToSender(createOffer(request))
    case request: GetOfferCodeRequest => replyToSender(handleGetOfferCode(request))
    case request: OfferValidate => replyToSender(helperValidateCode(request))
    case request: OfferConsume => replyToSender(handleConsumeCode(request))
    case request: SaleCreate => replyToSender(handleSaleCreate(request))
    case request: SaleRequestDetail => replyToSender(handleSaleDetail(request))
    case request: SaleComplete => replyToSender(handleSaleComplete(request))
  }

  def handleSaleCreate(create: SaleCreate): Future[ResponseWithFailure[OfferError, SaleResponse]] = successful {
    saleDAO.insertNew(KaredoSales(
      saleType="SALE",
      accountId=create.accountId,
      points=create.points,
      dateExpires = new DateTime().plus(create.expireInSeconds*1000))) match {
      case Some(x) => SuccessResponse(SaleResponse(x))
      case None => FailureResponse(InvalidOfferRequest("Can't create sale"))
    }
  }
  def handleSaleComplete(request: SaleComplete): Future[ResponseWithFailure[OfferError, SaleResponse]] = successful {
   saleDAO.findById(request.saleId) match {
     case None => FailureResponse(InvalidOfferRequest("sale id not existent"))
     case Some(sale) =>
       if(sale.saleType!="SALE") FailureResponse(InvalidOfferRequest("sale referring to invalid type"))
       else
         saleDAO.complete(request.saleId) match {
           case Some(sale) => SuccessResponse(SaleResponse(sale.id))
           case None => FailureResponse(InvalidOfferRequest("cannot complete the sale"))
         }
   }
  }

  def handleSaleDetail(request: SaleRequestDetail): Future[ResponseWithFailure[OfferError, SaleDetail]] = successful {
    saleDAO.findById(request.saleId) match {
      case None => FailureResponse(InvalidOfferRequest("Can't find sale"))
      case Some(sale) =>
        if (sale.saleType != "SALE") FailureResponse(InvalidOfferRequest("Id illegally refers to an offer"))
        else {
          customerDAO.getById(sale.accountId) match {
            case Some(merchant) => SuccessResponse(SaleDetail(merchant.personalInfo.name, sale.points))
            case None => FailureResponse(InvalidOfferRequest("Can't find merchant referred by sale"))

          }
        }
    }
  }

  def handleConsumeCode(consume: OfferConsume): Future[ResponseWithFailure[OfferError, OfferResponse]] = successful {
    saleDAO.consume(consume.offerCode) match {
      case None => FailureResponse(InvalidOfferRequest("Code invalid"))
      case Some(sale) =>
        brandDAO.getAdById(sale.adId.get) match {
          case Some(offer) =>
            customerDAO.consume(sale.accountId, offer.value)
            SuccessResponse(OfferResponse(sale.id))
          case None =>FailureResponse(InvalidOfferRequest("Cant find offer"))
        }
    }
  }
  def helperValidateCode(request: OfferValidate):
            Future[ResponseWithFailure[OfferError,OfferResponse]]= successful {
    saleDAO.findByCode(request.offerCode) match {
      case None => FailureResponse(InvalidOfferRequest("Code invalid"))
      case Some(sale) => SuccessResponse(OfferResponse(sale.id))
    }
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


  def handleGetOfferCode(request: GetOfferCodeRequest): Future[ResponseWithFailure[OfferError, GetOfferCodeResponse]] =
  successful {
    def getUnusedCode: String = {
      var code=""
      do {
        code = newOfferCode

      } while (saleDAO.findByCode(code)!=None)
      code
    }
    def newOfferCode: String = {
      RandomStringUtils.randomAlphanumeric(8).toUpperCase()
    }
    brandDAO.getAdById(request.adId) match {
        case Some(offer) =>
          // returns a randomcode only if NOT already used by other offers
          val code = getUnusedCode

          saleDAO.insertNew(KaredoSales(
          accountId = request.userId,
          points = offer.value,
          saleType = "OFFER",
          adId = Some(request.adId),
          code = Some(code))) match {
            case Some(x) => SuccessResponse(GetOfferCodeResponse(x, code))
            case _ => FailureResponse(InvalidOfferRequest("Can't create code"))
          }
        case None => FailureResponse(InvalidOfferRequest("Can't find offer"))
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
