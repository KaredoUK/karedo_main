package com.parallelai.wallet.datamanager.data

case class RegistrationRequest(applicationId: ApplicationID, msisdn: Option[String], email: Option[String])
case class RegistrationValidation(applicationId: ApplicationID, validationCode: String)
