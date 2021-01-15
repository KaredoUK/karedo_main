package com.parallelai.wallet.datamanager.data

/**
 * These are the types to be found as request/response API layer
 * these are probably also linked in the ApiDataJsonProtocol object implicits
 * to be serialized/unserialized in JSON
 */
import java.util.UUID
import org.joda.time.DateTime
import com.wordnik.swagger.annotations._
import scala.annotation.meta.field


// if a type acts either as a request and response can extends both

sealed trait ApiDataRequest // to mark all requests in this file
sealed trait ApiDataResponse // to mark responses

//
// Registration/User section
//
trait WithUserContacts {
  def msisdn: Option[String]
  def email: Option[String]

  def isValid : Boolean = msisdn.isDefined || email.isDefined
}

@ApiModel(description = "The registration request. To be valid at least one of the MSISDN or the Email needs to be provided")
case class RegistrationRequest(
                                @(ApiModelProperty@field)(value = "unique identifier for the application")
                                applicationId: UUID,
                                @(ApiModelProperty@field)(value = "User msisdn (optional)")
                                msisdn: Option[String],
                                @(ApiModelProperty@field)(value = "User email (optional)")
                                email: Option[String])

  extends WithUserContacts with ApiDataRequest

@ApiModel(description = "Credentials when trying to login")
case class APILoginRequest(
                            @(ApiModelProperty@field)(value = "password")
                            password: String)
  extends ApiDataRequest

@ApiModel(description = "Returned SessionId to be used as Header for subsequent accesses")
case class APISessionResponse(
                               @(ApiModelProperty@field)(value = "returned sessionId")
                               sessionId: String)
  extends ApiDataResponse

@ApiModel(description = "Application request")
case class AddApplicationRequest(
                                  @(ApiModelProperty@field)(value = "unique identifier for the application")
                                  applicationId: ApplicationID,
                                  @(ApiModelProperty@field)(value = "User msisdn (optional)")
                                  msisdn: Option[String],
                                  @(ApiModelProperty@field)(value = "User email (optional)")
                                  email: Option[String])
  extends WithUserContacts with ApiDataRequest

@ApiModel(description = "Data to be validated from previous application")
case class RegistrationValidation(
                                   @(ApiModelProperty@field)(value = "unique identifier for the application")
                                   applicationId: ApplicationID,
                                   @(ApiModelProperty@field)(value = "validation code sent by email/sms")
                                   validationCode: String,
                                   @(ApiModelProperty@field)(value = "user password to be set")
                                   password: Option[String] = None)
  extends ApiDataRequest

@ApiModel(description = "Data returned on first application (or reset of application)")
case class RegistrationResponse(
                                 @(ApiModelProperty@field)(value = "unique identifier for the application")
                                 applicationId: ApplicationID,
                                 @(ApiModelProperty@field)(value = "which channel used for sending activation code (sms/email)")
                                 channel: String,
                                 @(ApiModelProperty@field)(value = "address")
                                 address: String)
  extends ApiDataResponse

@ApiModel(description = "Add application response")
case class AddApplicationResponse(
                                   @(ApiModelProperty@field)(value = "unique identifier for the application")
                                   applicationId: ApplicationID,
                                   @(ApiModelProperty@field)(value = "which channel used for sending activation code (sms/email)")
                                   channel: String,
                                   @(ApiModelProperty@field)(value = "address")
                                   address: String)
  extends ApiDataResponse

@ApiModel(description = "Returns user data for successful validation")
case class RegistrationValidationResponse(
                                           @(ApiModelProperty@field)(value = "unique identifier for the application")
                                           applicationId: ApplicationID,
                                           @(ApiModelProperty@field)(value = "userId")
                                           userID: UUID)
  extends ApiDataResponse

@ApiModel(description = "General User Info")
case class UserSettings(
                         @(ApiModelProperty@field)(value = "Max ads allowable for each week")
                         maxAdsPerWeek: Int)

@ApiModel(description = "General User Info")
case class UserInfo(
                     @(ApiModelProperty@field)(value = "userId")
                     userId: UserID,
                     @(ApiModelProperty@field)(value = "full name")
                     fullName: String,
                     @(ApiModelProperty@field)(value = "email")
                     email: Option[String],
                     @(ApiModelProperty@field)(value = "phone number")
                     msisdn: Option[String],
                     @(ApiModelProperty@field)(value = "postal code")
                    postCode: Option[String],
                     @(ApiModelProperty@field)(value = "country")
                     country: Option[String],
                     @(ApiModelProperty@field)(value = "birth date")
                    birthDate: Option[DateTime],
                     @(ApiModelProperty@field)(value = "gender")
                     gender: Option[String]) extends WithUserContacts

@ApiModel(description = "Returned Information about the user")
case class UserProfile(
                        @(ApiModelProperty@field)(value = "user info")
                        info: UserInfo,
                        @(ApiModelProperty@field)(value = "user settings")
                        settings: UserSettings,
                        @(ApiModelProperty@field)(value = "total points")
                        totalPoints: Long)

@ApiModel(description = "Points gained by the user")
case class UserPoints(
                       @(ApiModelProperty@field)(value = "userId")
                       userId: UserID,
                       @(ApiModelProperty@field)(value = "total points gained")
                       totalPoints: Long)

case class LoginRequest(accountId: UserID, applicationId: UUID, password: String)

case class UserContacts(email: Option[String], msisdn: Option[String]) extends WithUserContacts

@ApiModel(description = "Status of the response, usually OK")
case class RestResponse(status:String)
//
// BRAND section
//
case object ListBrands extends ApiDataResponse

@ApiModel(description = "Brand Data Info")
case class BrandRecord(
                        @(ApiModelProperty@field)(value = "BrandId")
                        id: UUID,
                        @(ApiModelProperty@field)(value = "Brand Name")
                        name: String,
                        @(ApiModelProperty@field)(value = "Brand Icon")
                        iconId: String) extends ApiDataResponse
@ApiModel(description = "Brand Data to create")
case class BrandData(
                      @(ApiModelProperty@field)(value = "Brand Name")
                      name: String,
                      @(ApiModelProperty@field)(value = "Icon Image reference")
                      iconId: String ) extends ApiDataRequest

@ApiModel(description = "Created Brand")
case class BrandResponse(
                          @(ApiModelProperty@field)(value = "UUID of the newly brand created")
                          id: UUID) extends ApiDataResponse
@ApiModel(description = "Brand to add")
case class BrandIDRequest(
                           @(ApiModelProperty@field)(value = "brandId")
                           brandId: UUID) extends ApiDataRequest
case class DeleteBrandRequest(brandId: UUID) extends ApiDataRequest

// BRAND ADS
// This is here just to please the UI devs who are having problem to parse a list of Strings
case class ImageId(imageId: String)

case class AddAdvertCommand(brandId: UUID, text: String, imageIds: List[ImageId], value: Int) extends ApiDataRequest

@ApiModel(description = "Detail of returned Ad")
case class AdvertDetail(
                         @(ApiModelProperty@field)(value = "ad text")
                         text: String,
                         @(ApiModelProperty@field)(value = "list of images")
                         imageIds: List[ImageId],
                         @(ApiModelProperty@field)(value = "points weight of this ad")
                         value: Int)

@ApiModel(description = "Detail of returned Ad")
case class AdvertDetailResponse(
                                 @(ApiModelProperty@field)(value = "detail UUID")
                                 id: UUID,
                                 @(ApiModelProperty@field)(value = "text of the ad")
                                 text: String,
                                 @(ApiModelProperty@field)(value = "listing of images")
                                 imageIds: List[ImageId],
                                 @(ApiModelProperty@field)(value = "points weight of this ad")
                                 value: Int) extends ApiDataResponse

@ApiModel(description = "Brand to add")
case class SuggestedAdForUsersAndBrand(
                                        @(ApiModelProperty@field)(value = "adId")
                                        id: UUID,
                                        @(ApiModelProperty@field)(value = "adName")
                                        name: String,
                                        @(ApiModelProperty@field)(value = "iconimage")
                                        iconId: String) extends ApiDataResponse
case class RequestSuggestedAdForUsersAndBrand(userId: UUID, brandId: UUID, max: Int) extends ApiDataRequest
case class ListBrandsAdverts(brandId: UUID, max:Int=0) extends ApiDataRequest
case class ListbrandsAdvertsTemp(brandId: UUID)
case class GetBrandAdvert(brandId:UUID, adId:UUID) extends ApiDataRequest
case class DeleteAdvRequest(brandId: UUID, advId: UUID) extends ApiDataRequest


// MEDIA
case class AddMediaRequest(name: String, contentType: String, bytes: Array[Byte]) extends ApiDataRequest
case class AddMediaResponse(mediaId: String) extends ApiDataResponse

case class GetMediaRequest(mediaId: String) extends ApiDataRequest
case class GetMediaResponse(contentType: String, content: Array[Byte]) extends ApiDataResponse

// Offer types
case class OfferData(name: String, brandId: UUID, desc: Option[String], imagePath: Option[String], qrCodeId: Option[UUID], value: Option[Long])
case class OfferResponse(offerId: UUID)

case class StatusResponse(status: String) extends ApiDataResponse

// Other types
case class UserBrandInteraction(userId:UUID, brandId: UUID, intType: String) extends ApiDataRequest
@ApiModel(description = "Interaction Response")
case class InteractionResponse(
                                @(ApiModelProperty@field)(value = "userId")
                                userId: UUID,
                                @(ApiModelProperty@field)(value = "total points so far")
                                userTotalPoints: Int) extends ApiDataResponse
