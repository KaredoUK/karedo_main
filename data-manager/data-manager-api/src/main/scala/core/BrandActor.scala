package core

import java.io.ByteArrayInputStream
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.parallelai.wallet.datamanager.data._
import objAPI._

import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import parallelai.wallet.entity.{Brand, _}
import parallelai.wallet.persistence._
import rules.ComputePoints
import spray.json._

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global


class BrandActor()
                (implicit val bindingModule: BindingModule,
                 implicit val userAccountDAO: UserAccountDAO,
                 implicit val brandDAO: BrandDAO,
                 implicit val logDAO: LogDAO,
                 implicit val hintDAO: HintDAO)

  extends Actor with ActorLogging with Injectable with ISODateConversion {

  def receive: Receive = {
    case request: BrandData => replyToSender(handleCreateBrand(request))
    case request: AddAdvertCommand => replyToSender(handleAddAdvert(request))
    case ListBrands => sender ! handleListBrands
    case request: BrandIDRequest => replyToSender(handleGetBrand(request))
    case request: DeleteBrandRequest => replyToSender(handleDeleteBrand(request))
    case request: DeleteAdvRequest => replyToSender(handleDeleteAdv(request))
    case request: ListBrandsAdverts => replyToSender(handleListBrandAdverts(request))
    case request: GetBrandAdvert => replyToSender(handleGetBrandAdvert(request))
    case request: RequestSuggestedAdForUsersAndBrand => sender ! handleReturnSuggestedAds(request)
    case request: UserBrandInteraction => replyToSender(handleBrandInteraction(request))
    case request: UserOfferInteraction => replyToSender(handleOfferInteraction(request))

  }

  def handleBrandInteraction(interaction: UserBrandInteraction)
  : Future[ResponseWithFailure[APIError, InteractionResponse]] = successful {
    val brand = interaction.brandId
    brandDAO.getById(brand) match {
      case Some(brand) => handleValidBrand(interaction)
      case _ => FailureResponse(InvalidRequest("Brand not found"))
    }
  }

  def handleOfferInteraction(interaction: UserOfferInteraction)
  : Future[ResponseWithFailure[APIError, InteractionResponse]] = successful {
    val offer = interaction.offerId
    brandDAO.getAdById(offer) match {
      case Some(_) => handleValidOffer(interaction)
      case _ => FailureResponse(InvalidRequest("Offer not found"))
    }
  }
  def handleValidOffer(interaction: UserOfferInteraction): ResponseWithFailure[APIError, InteractionResponse] = {
    val points = ComputePoints.GetInteractionPoints(interaction)
    val user = interaction.userId
    val offer = interaction.offerId
    val intType = interaction.interaction
    val intSubType = interaction.intType
    userAccountDAO.addPoints(user, points) match {
      case Some(p: UserAccountTotalPoints) => {

        val l: KaredoLog = KaredoLog(
          user = Some(user),
          offer = Some(offer),
          logType = Some(intType + " " + intSubType),
          text = s"interacted total points: $p")

        logDAO.addLog(l)
        val response = InteractionResponse(interaction.userId, p.totalPoints.toLong)
        SuccessResponse(response)
      }
      case _ => {
        FailureResponse(InvalidRequest("User not found"))
      }
    }

  }

  def handleValidBrand(interaction: UserBrandInteraction): ResponseWithFailure[APIError, InteractionResponse] = {
    val points = ComputePoints.GetInteractionPoints(interaction)
    val user = interaction.userId
    val brand = interaction.brandId
    val intType = interaction.interaction
    val intSubType = interaction.intType
    userAccountDAO.addPoints(user, points) match {
      case Some(p: UserAccountTotalPoints) => {

        val l: KaredoLog = KaredoLog(
          user = Some(user),
          brand = Some(brand),
          logType = Some(intType + " " + intSubType),
          text = s"interacted total points: $p")

        logDAO.addLog(l)
        val response = InteractionResponse(interaction.userId, p.totalPoints.toLong)
        SuccessResponse(response)
      }
      case _ => {
        FailureResponse(InvalidRequest("User not found"))
      }
    }

  }

  def handleCreateBrand(request: BrandData): Future[ResponseWithFailure[APIError, BrandResponse]] = successful {

    validateBrand(request) match {
      case None =>
        log.info("Creating new brand for request {}", request)
        val newbrand = Brand(name = request.name,
          startDate=DateTime.parse(request.startDate),
          endDate=DateTime.parse(request.endDate),
          iconId = request.iconId, ads = List[AdvertisementDetail]())
        val uuid = brandDAO.insertNew(newbrand).get
        val response = BrandResponse(uuid)
        SuccessResponse(response)

      case Some(error) =>
        log.info("Validation failed " + error)
        FailureResponse(InvalidRequest(error))
    }
  }

  def handleReturnSuggestedAds(request: RequestSuggestedAdForUsersAndBrand): List[SuggestedAdForUsersAndBrand] = {

    hintDAO.suggestedNAdsForUserAndBrandLimited(request.userId, request.brandId, request.max)
      .map(x => {

      val adDetail = brandDAO.getAdById(x.ad).getOrElse(
        AdvertisementDetail(id = new UUID(0, 0), imageIds = List("empty")))
      SuggestedAdForUsersAndBrand(x.ad, adDetail.text, adDetail.imageIds.mkString(","))
    })

  }

  def handleListBrandAdverts(adverts: ListBrandsAdverts): Future[ResponseWithFailure[APIError, List[AdvertDetailResponse]]] =
    successful {
      SuccessResponse(
        brandDAO.listAds(adverts.brandId, adverts.max).map {
          detail => AdvertDetailResponse(id = detail.id, text = detail.text, imageIds = detail.imageIds map {
            ImageId
          }, value = detail.value)
        })
    }


  def handleGetBrandAdvert(advert: GetBrandAdvert): Future[ResponseWithFailure[APIError, AdvertDetailResponse]] =
    successful {
      brandDAO.getAdById(advert.adId) match {
        case Some(detail) => SuccessResponse(AdvertDetailResponse(detail.id, detail.text, detail.imageIds.map(ImageId(_)), detail.value))
        case None => FailureResponse(InvalidRequest("Invalid adId"))
      }
    }

  def handleDeleteAdv(request: DeleteAdvRequest): Future[ResponseWithFailure[APIError, String]] = successful {
    brandDAO.delAd(request.advId)

    SuccessResponse("OK")
  }

  def handleAddAdvert(request: AddAdvertCommand): Future[ResponseWithFailure[APIError, AdvertDetailResponse]] = successful {

    val detail: AdvertisementDetail = AdvertisementDetail(text = request.text, imageIds = request.imageIds map {
      _.imageId
    }, value = request.value)
    //log.info(s"XXX using detail uuid: ${detail.id}")
    brandDAO.addAd(request.brandId, detail)
    SuccessResponse(AdvertDetailResponse(detail.id, request.text, request.imageIds, request.value))
  }


  def handleListBrands: List[BrandRecord] = {

    val list = brandDAO.list.map(b => BrandRecord(b.id, b.name,
      b.createDate,
      b.startDate,
      b.endDate,
      b.iconId))
    log.info(s" returning a list of ${list.size} brands")
    list

  }



  def handleGetBrand(request: BrandIDRequest): Future[ResponseWithFailure[APIError, BrandRecord]] = successful {
    brandDAO.getById(request.brandId) match {
      case Some(b) => {
        log.info(s"getting brand ${b.id}")
        SuccessResponse(BrandRecord(b.id, b.name,
          b.createDate,
          b.startDate,
          b.endDate,
          b.iconId))
      }
      case None => {
        log.info(s" cannot get brand ${request.brandId}")
        FailureResponse(InvalidRequest("Invalid id"))
      }
    }
  }

  def handleDeleteBrand(request: DeleteBrandRequest): Future[ResponseWithFailure[APIError, String]] = successful {
    brandDAO.getById(request.brandId) match {
      case Some(b) => {
        log.info(s"deleting brand ${request.brandId}")
        brandDAO.delete(request.brandId)
        SuccessResponse("")
      }
      case None => {
        log.info(s"cannot delete brand ${request.brandId}")
        FailureResponse(InvalidRequest("Invalid brand id"))
      }
    }
  }


  def validateBrand(request: BrandData): Option[String] = request match {
    case BrandData("", _, _, _) => Some("Name must be not empty")
    case _ => None
  }

  def replyToSender[T <: Any](response: Future[ResponseWithFailure[APIError, T]]): Unit = {
    val replyTo = sender

    response recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InternalError(t))
    } foreach {
      responseContent: ResponseWithFailure[APIError, T] =>
        replyTo ! responseContent
    }
  }

}
