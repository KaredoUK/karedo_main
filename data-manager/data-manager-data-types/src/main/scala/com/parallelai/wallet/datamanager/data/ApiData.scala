package com.parallelai.wallet.datamanager.data

import java.util.UUID

case class RegistrationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])
case class RegistrationValidation(applicationId: ApplicationID, validationCode: String)

case class RegistrationResponse(applicationId: ApplicationID, channel: String, address: String)
case class RegistrationValidationResponse(applicationId: ApplicationID, userID: UUID)

case class UserSettings(maxAdsPerDay: Int)
case class UserInfo(fullName: String, email: Option[String], msisdn: Option[String], address: Option[String], postCode: Option[String], country: Option[String])

case class UserProfile(info: UserInfo, settings: UserSettings)
