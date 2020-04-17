package com.parallelai.wallet.datamanager.data

import spray.json._
import spray.json.DefaultJsonProtocol
import java.util.UUID

object ApiDataJsonProtocol extends DefaultJsonProtocol {

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x           => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

  implicit val registrationRequestJson = jsonFormat3(RegistrationRequest)
  implicit val registrationValidationJson = jsonFormat2(RegistrationValidation)
  implicit val registrationResponseJson = jsonFormat3(RegistrationResponse)
  implicit val registrationValidationResponseJson = jsonFormat2(RegistrationValidationResponse)

  implicit val userSettingsJson = jsonFormat1(UserSettings)
  implicit val userInfoJson = jsonFormat7(UserInfo)
  implicit val userProfileJson = jsonFormat2(UserProfile)

}
