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

case class APILoginRequest(password: String)
  extends ApiDataRequest

case class APISessionResponse(sessionId: String)
  extends ApiDataResponse

case class AddApplicationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])
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

case class AddApplicationResponse(applicationId: ApplicationID, channel: String, address: String)
  extends ApiDataResponse

@ApiModel(description = "Returns user data for successful validation")
case class RegistrationValidationResponse(
                                           @(ApiModelProperty@field)(value = "unique identifier for the application")
                                           applicationId: ApplicationID,
                                           @(ApiModelProperty@field)(value = "userId")
                                           userID: UUID)
  extends ApiDataResponse

case class UserSettings(maxAdsPerWeek: Int)
case class UserInfo(userId: UserID, fullName: String, email: Option[String], msisdn: Option[String],
                    postCode: Option[String], country: Option[String],
                    birthDate: Option[DateTime], gender: Option[String]) extends WithUserContacts

case class UserProfile(info: UserInfo, settings: UserSettings, totalPoints: Long)

case class UserPoints(userId: UserID, totalPoints: Long)

case class LoginRequest(accountId: UserID, applicationId: UUID, password: String)

case class UserContacts(email: Option[String], msisdn: Option[String]) extends WithUserContacts

//
// BRAND section
//
case object ListBrands extends ApiDataResponse

case class BrandRecord(id: UUID, name: String, iconId: String) extends ApiDataResponse
case class BrandData(name: String, iconId: String ) extends ApiDataRequest
case class BrandResponse(id: UUID) extends ApiDataResponse
case class BrandIDRequest(brandId: UUID) extends ApiDataRequest
case class DeleteBrandRequest(brandId: UUID) extends ApiDataRequest

// BRAND ADS
// This is here just to please the UI devs who are having problem to parse a list of Strings
case class ImageId(imageId: String)

case class AddAdvertCommand(brandId: UUID, text: String, imageIds: List[ImageId], value: Int) extends ApiDataRequest
case class AdvertDetail(text: String, imageIds: List[ImageId], value: Int)
case class AdvertDetailResponse(id: UUID, text: String, imageIds: List[ImageId], value: Int) extends ApiDataResponse
case class SuggestedAdForUsersAndBrand(id: UUID, name: String, iconId: String) extends ApiDataResponse
case class RequestSuggestedAdForUsersAndBrand(userId: UUID, brandId: UUID, max: Int) extends ApiDataRequest
case class ListBrandsAdverts(brandId: UUID) extends ApiDataRequest
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
case class InteractionResponse(userId: UUID, userTotalPoints: Int) extends ApiDataResponse
