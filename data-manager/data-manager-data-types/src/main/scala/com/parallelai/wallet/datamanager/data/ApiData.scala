package com.parallelai.wallet.datamanager.data

/**
 * These are the types to be found as request/response API layer
 * these are probably also linked in the ApiDataJsonProtocol object implicits
 * to be serialized/unserialized in JSON
 */
import java.util.UUID
import org.joda.time.DateTime

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


case class RegistrationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])
  extends WithUserContacts with ApiDataRequest
case class AddApplicationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])
  extends ApiDataRequest

case class RegistrationValidation(applicationId: ApplicationID, validationCode: String)
  extends ApiDataRequest

case class RegistrationResponse(applicationId: ApplicationID, channel: String, address: String)
  extends ApiDataResponse
case class RegistrationValidationResponse(applicationId: ApplicationID, userID: UUID)
  extends ApiDataResponse

case class UserSettings(maxAdsPerWeek: Int)
case class UserInfo(userId: UserID, fullName: String, email: Option[String], msisdn: Option[String],
                    postCode: Option[String], country: Option[String],
                    birthDate: Option[DateTime], gender: Option[String]) extends WithUserContacts

case class UserProfile(info: UserInfo, settings: UserSettings, totalPoints: Long)

case class UserPoints(userId: UserID, totalPoints: Long)


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
// This is heare just to please the UI devs who are aving problem to parse a list of Strings
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
case class InteractionType(interactionType: String) extends ApiDataRequest
case class InteractionResponse(userId: UUID, userTotalPoints: Int)
