package com.parallelai.wallet.datamanager.data

import java.util.UUID
import org.joda.time.DateTime

trait WithUserContacts {
  def msisdn: Option[String]
  def email: Option[String]

  def isValid : Boolean = msisdn.isDefined || email.isDefined
}

case class RegistrationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String], password: String) extends WithUserContacts
case class AddApplicationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])

case class RegistrationValidation(applicationId: ApplicationID, validationCode: String)

case class RegistrationResponse(applicationId: ApplicationID, channel: String, address: String)
case class RegistrationValidationResponse(applicationId: ApplicationID, userID: UUID)

case class UserSettings(maxAdsPerWeek: Int, password: String)
case class UserInfo(userId: UserID, fullName: String, email: Option[String], msisdn: Option[String],
                    postCode: Option[String], country: Option[String],
                    birthDate: Option[DateTime], gender: Option[String]) extends WithUserContacts

case class UserProfile(info: UserInfo, settings: UserSettings)

case class UserContacts(email: Option[String], msisdn: Option[String]) extends WithUserContacts