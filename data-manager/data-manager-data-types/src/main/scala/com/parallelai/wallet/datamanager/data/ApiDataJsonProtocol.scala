package com.parallelai.wallet.datamanager.data

import com.parallelai.wallet.datamanager.data._

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.json._
import java.util.UUID

object ApiDataJsonProtocol extends DefaultJsonProtocol  {

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x           => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

  implicit object jodaDateTimeFormat extends RootJsonFormat[DateTime] {
    //2013-12-17
    val DATE_FORMAT = DateTimeFormat.forPattern("dd-MM-yyyy")

    def write(obj: DateTime): JsValue = JsString(DATE_FORMAT.print(obj))

    def read(json: JsValue): DateTime = {
      json match {
        case JsString(date) => DATE_FORMAT.parseDateTime(date)

        case _ => throw new IllegalArgumentException(s"Expected JsString with content having format $DATE_FORMAT for a DateTime attribute value")
      }
    }
  }

  implicit val registrationRequestJson = jsonFormat3(RegistrationRequest)
  implicit val registrationValidationJson = jsonFormat2(RegistrationValidation)
  implicit val registrationResponseJson = jsonFormat3(RegistrationResponse)
  implicit val registrationValidationResponseJson = jsonFormat2(RegistrationValidationResponse)

  implicit val userSettingsJson = jsonFormat1(UserSettings)
  implicit val userInfoJson = jsonFormat8(UserInfo)
  implicit val userProfileJson = jsonFormat3(UserProfile)

  implicit val userPointsJson = jsonFormat2(UserPoints)

  implicit val brandDataJson = jsonFormat2(BrandData)
  implicit val brandRecordJson = jsonFormat3(BrandRecord)
  implicit val uuidJson = jsonFormat1(BrandResponse)
  implicit val brandIdJson = jsonFormat1(BrandIDRequest)
  implicit val statusJson = jsonFormat1(StatusResponse)
  implicit val advDetailJson = jsonFormat4(AdvertisementDetailResponse)


}
