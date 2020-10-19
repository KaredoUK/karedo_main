package core

import java.io.ByteArrayInputStream
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.parallelai.wallet.datamanager.data._
import core.BrandActor.{BrandError, InternalBrandError, InvalidBrandRequest}
import org.joda.time.DateTime
import parallelai.wallet.entity.{Brand, _}
import parallelai.wallet.persistence.{HintDAO, MediaDAO, BrandDAO}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * We use the companion object to hold all the messages that the ``BrandActor``
 * receives.
 */
object BrandActor {

  def props(brandDAO: BrandDAO, hintDAO: HintDAO)(implicit bindingModule: BindingModule): Props =
    Props(classOf[BrandActor], brandDAO, hintDAO, bindingModule)


  sealed trait BrandError

  case class InvalidBrandRequest(reason: String) extends BrandError

  case class InternalBrandError(reason: Throwable) extends BrandError

  implicit object brandErrorJsonFormat extends RootJsonWriter[BrandError] {
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


  }

}

class BrandActor(brandDAO: BrandDAO, hintDAO: HintDAO)
                (implicit val bindingModule: BindingModule) extends Actor with ActorLogging with Injectable {


  def receive: Receive = {
    case request: BrandData => replyToSender(createBrand(request))
    case request: AddAdvertCommand => replyToSender(addAdvert(request))
    case ListBrands => sender ! listBrands
    case request: BrandIDRequest => replyToSender(getBrand(request))
    case request: DeleteBrandRequest => replyToSender(deleteBrand(request))
    case request: DeleteAdvRequest => replyToSender(deleteAdv(request))
    case request: ListBrandsAdverts => replyToSender(listBrandAdverts(request))
    case request: RequestSuggestedAdForUsersAndBrand => sender ! returnSuggestedAds(request)

  }

  def returnSuggestedAds(request: RequestSuggestedAdForUsersAndBrand): List[SuggestedAdForUsersAndBrand] = {

    hintDAO.suggestedNAdsForUserAndBrandLimited(request.userId, request.brandId, request.max)
      .map(x => {

      val adDetail = brandDAO.getAdById (x.ad).getOrElse(
        AdvertisementDetail(id=new UUID(0,0),imageIds = List("empty")))
      SuggestedAdForUsersAndBrand (x.ad, adDetail.text, adDetail.imageIds.mkString(","))
    })

  }

  def listBrandAdverts(adverts: ListBrandsAdverts): Future[ResponseWithFailure[BrandError, List[AdvertDetailResponse]]] =
    successful {
      SuccessResponse(
        brandDAO.listAds(adverts.brandId).map {
          detail => AdvertDetailResponse(id = detail.id, text = detail.text, imageIds = detail.imageIds map { ImageId } , value = detail.value)
        })
    }


  def deleteAdv(request: DeleteAdvRequest): Future[ResponseWithFailure[BrandError, String]] = successful {
    brandDAO.delAd(request.advId)

    SuccessResponse("OK")
  }

  def addAdvert(request: AddAdvertCommand): Future[ResponseWithFailure[BrandError, AdvertDetailResponse]] = successful {

    val detail: AdvertisementDetail = AdvertisementDetail(text = request.text, imageIds = request.imageIds map { _.imageId }, value = request.value)
    //log.info(s"XXX using detail uuid: ${detail.id}")
    brandDAO.addAd(request.brandId, detail)
    SuccessResponse(AdvertDetailResponse(detail.id, request.text, request.imageIds, request.value))
  }


  def createBrand(request: BrandData): Future[ResponseWithFailure[BrandError, BrandResponse]] = successful {

    validateBrand(request) match {
      case None =>
        log.info("Creating new brand for request {}", request)
        val newbrand = Brand(name = request.name, iconId = request.iconId, ads = List[AdvertisementDetail]())
        val uuid = brandDAO.insertNew(newbrand).get
        val response = BrandResponse(uuid)
        SuccessResponse(response)

      case Some(error) =>
        log.info("Validation failed " + error)
        FailureResponse(InvalidBrandRequest(error))
    }
  }

  def listBrands: List[BrandRecord] = {

    val list = brandDAO.list.map(b => BrandRecord(b.id, b.name, b.iconId))
    log.info(s" returning a list of ${list.size} brands")
    list

  }


  def getBrand(request: BrandIDRequest): Future[ResponseWithFailure[BrandError, BrandRecord]] = successful {
    brandDAO.getById(request.brandId) match {
      case Some(b) => {
        log.info(s"getting brand ${b.id}")
        SuccessResponse(BrandRecord(b.id, b.name, b.iconId))
      }
      case None => {
        log.info(s" cannot get brand ${request.brandId}")
        FailureResponse(InvalidBrandRequest("Invalid id"))
      }
    }
  }

  def deleteBrand(request: DeleteBrandRequest): Future[ResponseWithFailure[BrandError, String]] = successful {
    brandDAO.getById(request.brandId) match {
      case Some(b) => {
        log.info(s"deleting brand ${request.brandId}")
        brandDAO.delete(request.brandId)
        SuccessResponse("")
      }
      case None => {
        log.info(s"cannot delete brand ${request.brandId}")
        FailureResponse(InvalidBrandRequest("Invalid id"))
      }
    }
  }


  def validateBrand(request: BrandData): Option[String] = request match {
    case BrandData("", _) => Some("Name must be not empty")
    case _ => None
  }

  def replyToSender[T <: Any](response: Future[ResponseWithFailure[BrandError, T]]): Unit = {
    val replyTo = sender

    response recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InternalBrandError(t))
    } foreach {
      responseContent: ResponseWithFailure[BrandError, T] =>
        replyTo ! responseContent
    }
  }

}
