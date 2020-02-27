package com.parallelai.wallet.datamanager.data

case class RegistrationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])
case class RegistrationValidation(applicationId: ApplicationID, validationCode: String)

case class RegistrationResponse(applicationId: ApplicationID, channel: String, address: String)


case class UserSettings(maxAdsPerDay: Int)
case class UserInfo(fullName: String, email: Option[String], msisdn: Option[String], address: Option[String], postCode: Option[String], country: Option[String])

case class UserProfile(info: UserInfo, settings: UserSettings)
