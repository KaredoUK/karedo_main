package com.parallelai.wallet.datamanager.data

import com.parallelai.wallet.datamanager.data._

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.json._
import java.util.UUID

trait ApiDataJsonProtocol extends DefaultJsonProtocol {

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)

    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

  implicit object jodaDateTimeFormat extends RootJsonFormat[DateTime] {
    //2013-12-17
    val DATE_FORMAT = DateTimeFormat.forPattern("dd-MM-yyyy")

    def write(obj: DateTime): JsValue = JsString(DATE_FORMAT.print(obj))

    def read(json: JsValue): DateTime = {
      json match {
        case JsString(date) => DATE_FORMAT.parseDateTime(date)

        case _ => throw new IllegalArgumentException(s"Expected JsString with content having format $DATE_FORMAT for a DateTime attribute value")
      }
    }
  }

  case class RtbCurrency(value: BigDecimal, name: String)
  implicit object currencyJson extends JsonFormat[RtbCurrency] {
    // 1
    def write(currency: RtbCurrency) = {
      JsArray(JsNumber(currency.value), JsString(currency.name))
    }
    def read(json: JsValue): RtbCurrency = {
      json match {
        case JsArray(jsvector) =>
          val (JsNumber(value),JsString(name)) = (jsvector(0),jsvector(1))
          RtbCurrency(value,name)
        case _ => throw new IllegalArgumentException(s"Expected JsVector with content having currency money value and name of currency")
      }
    }


  }

  implicit val registrationRequestJson = jsonFormat4(RegistrationRequest)
  implicit val addApplicationRequestJson = jsonFormat4(AddApplicationRequest)
  implicit val registrationValidationJson = jsonFormat3(RegistrationValidation)
  implicit val registrationResponseJson = jsonFormat3(RegistrationResponse)
  implicit val addApplicationResponseJson = jsonFormat3(AddApplicationResponse)
  implicit val registrationValidationResponseJson = jsonFormat2(RegistrationValidationResponse)
  implicit val registrationSessionResponse = jsonFormat1(APISessionResponse)
  implicit val loginRequest = jsonFormat1(APILoginRequest)

  implicit val userSettingsJson = jsonFormat1(UserSettings)
  implicit val userInfoJson = jsonFormat9(UserInfo)
  implicit val userProfileJson = jsonFormat3(UserProfile)
  implicit val userRestResponse = jsonFormat1(RestResponse)

  implicit val userPointsJson = jsonFormat2(UserPoints)

  implicit val brandDataJson = jsonFormat4(BrandData)
  implicit val brandInteractionJson = jsonFormat4(UserBrandInteraction)
  implicit val offerInteractionJson = jsonFormat4(UserOfferInteraction)
  implicit val brandRecordJson = jsonFormat6(BrandRecord)
  implicit val uuidJson = jsonFormat1(BrandResponse)
  implicit val brandIdJson = jsonFormat1(BrandIDRequest)
  implicit val DeleteBrandRequestJson = jsonFormat1(DeleteBrandRequest)
  implicit val SuggestedAdForUsersAndBrandJson = jsonFormat3(SuggestedAdForUsersAndBrand)

  implicit val offerDataJson = jsonFormat6(OfferData)
  implicit val offerResponseJson = jsonFormat1(OfferResponse)
  implicit val offerCodeJson = jsonFormat1(OfferCode)
  implicit val saleDetail = jsonFormat2(SaleDetail)
  implicit val saleComplete = jsonFormat2(SaleComplete)
  implicit val saleCreate = jsonFormat3(SaleCreate)
  implicit val saleResponse = jsonFormat1(SaleResponse)
  implicit val karedoChange = jsonFormat2(KaredoChange)
  implicit val currency = jsonFormat2(Currency)

  implicit val imageIdJson = jsonFormat1(ImageId)

  implicit val statusJson = jsonFormat1(StatusResponse)
  implicit val summaryImageApiJson = jsonFormat2(SummaryImageApi)
  implicit val advDetailResponseJson = jsonFormat9(AdvertDetailResponse)
  implicit val advListResponseJson = jsonFormat3(AdvertDetailListResponse)
  implicit val advSummaryResponseJson = jsonFormat6(AdvertSummaryResponse)

  implicit val advDetailJson = jsonFormat9(AdvertDetailApi)
  implicit val addMediaRequestJson = jsonFormat3(AddMediaRequest)
  implicit val addMediaResponseJson = jsonFormat1(AddMediaResponse)
  implicit val getMediaResponseJson = jsonFormat2(GetMediaResponse)


  implicit val getActiveAccountBrandOffersResponseJson = jsonFormat1(GetActiveAccountBrandOffersResponse)
  implicit val karedoSalesJson = jsonFormat9(KaredoSalesApi)


  implicit val interactionResponse = jsonFormat2(InteractionResponse)
  implicit val getOfferCodeRequest = jsonFormat2(GetOfferCodeRequest)
  implicit val getOfferCodeResponse = jsonFormat2(GetOfferCodeResponse)
  implicit val getOfferCode = jsonFormat2(Pakkio)



}
object ApiDataJsonProtocol extends ApiDataJsonProtocol

trait RtbJsonProtocol extends ApiDataJsonProtocol {

  case class Location(countryCode: String, regionCode: String, cityName: String, dma: Int = -1, metro: Int = -1, timezoneOffsetMinutes: Int = -1)

  case class UserId(prov: String, xchg: String)

  case class Impression(id: Int, formats: List[String], position: Int = 0)

  case class Spot(id: Int, formats: List[String], position: Int = 0)

  case class Rtb(id: Long, timestamp: String, isTest: Boolean = false, url: String, language: String, exchange: String,
                 location: Location, userIds: UserId, imp: List[Impression], spots: List[Spot])

  implicit val locationJson = jsonFormat6(Location)
  implicit val userIdJson = jsonFormat2(UserId)
  implicit val spotJson = jsonFormat3(Spot)
  implicit val impressionJson = jsonFormat3(Impression)
  implicit val rtbJson = jsonFormat10(Rtb)

  // "account":["hello", "world"],
  // "adSpotId":"1", "auctionId":"3438835179", "bidTimestamp":0.0, "channels":[], "timestamp":1470865069.551620,
  // "type":1, "uids":{"prov":"2046608033", "xchg":"678906006"}, "winPrice":[62, "USD/1M"]}
  
  case class Win(account: List[String], adSpotId: String, auctionId: String, bidTimestamp: BigDecimal,
                 channels: List[String], timestamp: Double, atype: Int, uids: UserId, winPrice: RtbCurrency)

  implicit val winJson = jsonFormat(Win, "account", "adSpotId", "auctionId", "bidTimestamp", "channels", "timestamp", "type", "uids", "winPrice")

}
