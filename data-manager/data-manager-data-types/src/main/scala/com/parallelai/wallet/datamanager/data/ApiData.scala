package com.parallelai.wallet.datamanager.data

import java.util.UUID
import org.joda.time.DateTime

trait WithUserContacts {
  def msisdn: Option[String]
  def email: Option[String]

  def isValid : Boolean = msisdn.isDefined || email.isDefined
}

case class RegistrationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String]) extends WithUserContacts
case class AddApplicationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])

case class RegistrationValidation(applicationId: ApplicationID, validationCode: String)

case class RegistrationResponse(applicationId: ApplicationID, channel: String, address: String)
case class RegistrationValidationResponse(applicationId: ApplicationID, userID: UUID)

case class UserSettings(maxAdsPerWeek: Int)
case class UserInfo(userId: UserID, fullName: String, email: Option[String], msisdn: Option[String],
                    postCode: Option[String], country: Option[String],
                    birthDate: Option[DateTime], gender: Option[String]) extends WithUserContacts

case class UserProfile(info: UserInfo, settings: UserSettings, totalPoints: Long)

case class UserPoints(userId: UserID, totalPoints: Long)


case class UserContacts(email: Option[String], msisdn: Option[String]) extends WithUserContacts

// BRAND
case object ListBrands

case class BrandRecord(id: UUID, name: String, iconId: String)
case class BrandData(name: String, iconId: String )
case class BrandResponse(id: UUID)
case class BrandIDRequest(brandId: UUID)
case class DeleteBrandRequest(brandId: UUID)

// BRAND ADS
case class AddAdvertCommand(brandId: UUID, text: String, imageIds: List[String], value: Int)
case class AdvertDetail(text: String, imageIds: List[String], value: Int)
case class AdvertDetailResponse(id: UUID, text: String, imageIds: List[String], value: Int)
case class ListBrandsAdverts(brandId: UUID)
case class DeleteAdvRequest(brandId: UUID, advId: UUID)


// MEDIA
case class AddMediaRequest(name: String, contentType: String, bytes: Array[Byte])
case class AddMediaResponse(mediaId: String)
case class GetMediaRequest(mediaId: String)
case class GetMediaResponse(contentType: String, content: Array[Byte])

// Offer types
case class OfferData(name: String, brandId: UUID, desc: Option[String], imagePath: Option[String], qrCodeId: Option[UUID], value: Option[Long])
case class OfferResponse(offerId: UUID)

case class StatusResponse(status: String)
