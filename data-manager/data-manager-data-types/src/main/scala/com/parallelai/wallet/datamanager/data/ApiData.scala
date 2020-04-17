package com.parallelai.wallet.datamanager.data

import java.util.UUID

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
                    address: Option[String], postCode: Option[String], country: Option[String]) extends WithUserContacts

case class UserProfile(info: UserInfo, settings: UserSettings)

case class UserContacts(email: Option[String], msisdn: Option[String]) extends WithUserContacts