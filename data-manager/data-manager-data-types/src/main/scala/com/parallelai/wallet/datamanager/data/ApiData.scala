package com.parallelai.wallet.datamanager.data

/**
 * These are the types to be found as request/response API layer
 * these are probably also linked in the ApiDataJsonProtocol object implicits
 * to be serialized/unserialized in JSON
 */
import java.util.UUID
import org.joda.time.DateTime
//import com.wordnik.swagger.annotations._
import org.joda.time.format.ISODateTimeFormat
import scala.annotation.meta.field

object KaredoTypes {
  type KaredoPoints = Long
}
import KaredoTypes._

// if a type acts either as a request and response can extends both

trait ApiDataRequest // to mark all requests in this file
trait ApiDataResponse // to mark responses

//
// Registration/User section
//
trait WithUserContacts {
  def msisdn: Option[String]
  def email: Option[String]

  def isValid : Boolean = msisdn.isDefined || email.isDefined
}

case class RegistrationRequest(
                                deviceId: UUID,
                                msisdn: Option[String],
                                userType: String,
                                email: Option[String])

  extends WithUserContacts with ApiDataRequest

case class APILoginRequest(
                            password: String)
  extends ApiDataRequest

case class APISessionResponse(
                               sessionId: String)
  extends ApiDataResponse

case class AddApplicationRequest(
                                  deviceId: DeviceID = UUID.randomUUID(),
                                  msisdn: Option[String] = None,
                                  userType: String = "",
                                  email: Option[String] = None)
  extends WithUserContacts with ApiDataRequest

case class RegistrationValidation(
                                   deviceId: DeviceID,
                                   validationCode: String,
                                   password: Option[String] = None)
  extends ApiDataRequest

case class RegistrationConfirmActivation(
                                          deviceId: DeviceID,
                                          userId: UserID
                                        )
case class RegistrationConfirmActivationResponse(
                                          deviceId: DeviceID,
                                          userId: UserID,
                                          password: String
                                        )



case class RegistrationResponse(
                                 deviceId: DeviceID,
                                 channel: String,
                                 address: String)
  extends ApiDataResponse

case class AddApplicationResponse(
                                   deviceId: DeviceID,
                                   channel: String,
                                   address: String)
  extends ApiDataResponse

case class RegistrationValidationResponse(
                                           deviceId: DeviceID,
                                           userID: UUID)
  extends ApiDataResponse

case class UserSettings(
                         maxAdsPerWeek: Int = 5)

case class UserInfo(
                     userId: UserID = UUID.randomUUID(),
                     userType: String = "USER",
                     fullName: String = "unspecified",
                     email: Option[String] = None,
                     msisdn: Option[String] = None,
                    postCode: Option[String] = None,
                     country: Option[String] = None,
                    birthDate: Option[String] = None,
                     gender: Option[String]=None) extends WithUserContacts

case class UserProfile(
                        info: UserInfo,
                        settings: UserSettings,
                        totalPoints: KaredoPoints)

                        
case class Intent(
    want: String = "",
    what: String = "",
    where: String = "")
    
case class Preferences(
    topics: List[String]=List())
                        
case class UserProfileExt(
  info: UserInfo = UserInfo(),
  intent: Intent = Intent(),
  preferences: Preferences = Preferences(),
  settings: UserSettings = UserSettings(),
  totalPoints: KaredoPoints = 0)

  
  

  
case class UserPoints(
                       userId: UserID,
                       totalPoints: KaredoPoints)

case class LoginRequest(accountId: UserID, deviceId: UUID, password: String)

case class UserContacts(email: Option[String], msisdn: Option[String]) extends WithUserContacts

case class RestResponse(status:String)
//
// BRAND section
//
case object ListBrands extends ApiDataResponse

case class BrandRecord(
                        id: UUID,
                        name: String,
                        createDate: String,
                        startDate: String,
                        endDate: String,
                        iconId: String) extends ApiDataResponse
case class BrandData(
                      name: String,
                      startDate: String = ISODateTimeFormat.dateTime().print(DateTime.now()),
                      endDate: String = ISODateTimeFormat.dateTime().print(DateTime.now().plusDays(10)),
                      iconId: String ) extends ApiDataRequest

case class BrandResponse(
                          id: UUID) extends ApiDataResponse
case class BrandIDRequest(
                           brandId: UUID) extends ApiDataRequest
case class DeleteBrandRequest(brandId: UUID) extends ApiDataRequest

// BRAND ADS
// This is here just to please the UI devs who are having problem to parse a list of Strings
case class ImageId(imageId: String)

case class AddAdvertCommand(
     brandId: UUID,
     shortText: String,
     detailedText: String,
     termsAndConditions: String,
     shareDetails: String,
     summaryImages:List[SummaryImageApi],
     startDate:String,
     endDate:String,
     imageIds: List[ImageId],
     karedos: KaredoPoints) extends ApiDataRequest

case class SummaryImageApi(
            imageId: String,
            imageType: Int
)

case class AdvertDetailApi(
                         shortText: String,
                         detailedText: String,
                         termsAndConditions: String,
                         shareDetails: String,
                         summaryImages: List[SummaryImageApi],
                         startDate: String = ISODateTimeFormat.dateTime().print(DateTime.now()),
                         endDate: String = ISODateTimeFormat.dateTime().print(DateTime.now().plusDays(10)),
                         imageIds: List[ImageId],
                         karedos: KaredoPoints)

case class AdvertDetailResponse(
                                 detailedText: String,
                                 termsAndConditions: String,
                                 shareDetails: String,
                                 startDate: String = ISODateTimeFormat.dateTime().print(DateTime.now()),
                                 endDate: String = ISODateTimeFormat.dateTime().print(DateTime.now().plusDays(10)),
                                 imageIds: List[ImageId],
                                 karedos: KaredoPoints,
                                 state: String,
                                 code: String



                                 ) extends ApiDataResponse

case class AdvertDetailListResponse(
                                 offerId: UUID,
                                 shortText: String,
                                 karedos: KaredoPoints
                                     )


case class AdvertSummaryResponse(

                                 shortText: String,
                                 summaryImages: List[SummaryImageApi],
                                 startDate: String = ISODateTimeFormat.dateTime().print(DateTime.now()),
                                 endDate: String = ISODateTimeFormat.dateTime().print(DateTime.now().plusDays(10)),

                                 karedos: KaredoPoints,

                                status: Boolean) extends ApiDataResponse

case class SuggestedAdForUsersAndBrand(
                                        id: UUID,
                                        name: String,
                                        iconId: String) extends ApiDataResponse
case class RequestSuggestedAdForUsersAndBrand(userId: UUID, brandId: UUID, max: Int) extends ApiDataRequest
case class ListBrandsAdverts(brandId: UUID, max:Int=0) extends ApiDataRequest
case class ListbrandsAdvertsTemp(brandId: UUID)
case class GetAdvertSummary(brandId:UUID, adId:UUID) extends ApiDataRequest
case class GetAdvertDetail(brandId:UUID, adId:UUID) extends ApiDataRequest
case class DeleteAdvRequest(brandId: UUID, advId: UUID) extends ApiDataRequest


// MEDIA
case class AddMediaRequest(name: String, contentType: String, bytes: Array[Byte]) extends ApiDataRequest
case class AddMediaResponse(mediaId: String) extends ApiDataResponse
case class GetActiveAccountBrandOffersResponse(
                                                numValidOffers: Int) extends ApiDataResponse


case class KaredoSalesApi(
                           id: UUID = UUID.randomUUID(),
                            saleType: String,
                           accountId: UUID,
                           adId: Option[UUID]=None,
                           code: Option[String]=None,
                           dateCreated: String,
                           dateExpires: String,
                           dateConsumed: Option[String] = None,
                           points: KaredoPoints
                        ) extends ApiDataResponse

case class GetMediaRequest(mediaId: String) extends ApiDataRequest
case class GetMediaResponse(contentType: String, content: Array[Byte]) extends ApiDataResponse
case class GetAccountBrandOffersResponse(numOfActiveOffers: Int)

// Offer types
case class OfferData(name: String, brandId: UUID, desc: Option[String], imagePath: Option[String], qrCodeId: Option[UUID], value: Option[Int])

case class OfferResponse(
    offerId: UUID)
    
case class OfferCode(
    offerCode: String)
case class OfferValidate(offerCode: String)
case class OfferConsume(offerCode: String)

case class SaleCreate(
    accountId: UUID,
    points: KaredoPoints,
    expireInSeconds: Long=0)
    
case class SaleResponse(
    saleId: UUID)

case class KaredoChange(
                         currency: String,
                         change: Double)
                         
case class Currency(
                         currency: String,
                         amount: Double)


case class SaleRequestDetail(saleId: UUID)
case class SaleDetail(merchantName: String, points: KaredoPoints)
case class SaleComplete(accountId: UUID, saleId: UUID)
case class RequestKaredoChange(currency: String)

case class GetOfferCodeRequest(userId: UUID, adId: UUID)
case class GetOfferCodeResponse(saleId: UUID, code: String)


case class StatusResponse(status: String) extends ApiDataResponse

// Other types
case class UserBrandInteraction(
                                 userId:UUID,
                                 brandId: UUID,
                                 interaction: String,
                                 intType: String="") extends ApiDataRequest
case class UserOfferInteraction(
                                 userId:UUID,
                                 offerId: UUID,
                                 interaction: String,
                                 intType: String="") extends ApiDataRequest
case class InteractionResponse(
                                userId: UUID,
                                userTotalPoints: KaredoPoints) extends ApiDataResponse

case class Pakkio(x:Int,s:String)

///////
