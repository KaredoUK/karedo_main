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
case class ListBrandsAdverts(brandId: UUID)
case class BrandRecord(id: UUID, name: String, iconId: UUID)
case class BrandData(name: String, iconId: UUID )
case class BrandResponse(id: UUID)
case class AddAdvertCommand(brandId: UUID, text: String, imageIds: List[UUID], value: Int)
case class AdvertDetail(text: String, imageIds: List[UUID], value: Int)
case class AdvertDetailResponse(id: UUID, text: String, imageIds: List[UUID], value: Int)
case class DeleteBrandRequest(brandId: UUID)
case class DeleteAdvRequest(brandId: UUID, advId: UUID)
case class BrandIDRequest(brandId: UUID)

// Offer types
case class OfferData(name: String, brandID: UUID, desc: String, imagePath: String, qrCodeId: UUID, value: Long)
case class OfferResponse(id: UUID)

case class StatusResponse(status: String)
