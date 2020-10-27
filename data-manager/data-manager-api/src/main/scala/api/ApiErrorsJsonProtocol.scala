package api


import core.objAPI.{InvalidRequest, APIError, InternalError}
import core.EditAccountActor._
import core.MediaContentActor.{MissingContent, InvalidContentId, MediaHandlingError}
import core.OfferActor.{InternalOfferError, InvalidOfferRequest, OfferError}
import core.RegistrationActor._
import spray.http.StatusCode
import spray.http.StatusCodes._

trait ApiErrorsJsonProtocol extends DefaultJsonFormats {
  implicit object registrationErrorSelector extends ErrorSelector[RegistrationError] {
    def apply(error: RegistrationError): StatusCode = error match {
      case InvalidRegistrationRequest(reason) => BadRequest
      case ApplicationAlreadyRegistered => BadRequest
      case UserAlreadyRegistered => BadRequest
      case InvalidValidationCode => Unauthorized
      case InternalRegistrationError(_) => InternalServerError
    }
  }

  implicit object errorSelector extends ErrorSelector[APIError] {
    def apply(error: APIError): StatusCode = error match {
      case InvalidRequest(reason) =>
        BadRequest
      case InternalError(_) =>
        InternalServerError
    }
  }

  implicit object mediaErrorSelector extends ErrorSelector[MediaHandlingError] {
    def apply(error: MediaHandlingError): StatusCode = error match {
      case InvalidContentId(_) =>
        BadRequest
      case MissingContent =>
        BadRequest
    }
  }

  implicit object offerErrorSelector extends ErrorSelector[OfferError] {
    def apply(error: OfferError): StatusCode = error match {
      case InvalidOfferRequest(reason) =>
        BadRequest
      case InternalOfferError(_) =>
        InternalServerError
    }
  }

  implicit object editAccountErrorSelector extends ErrorSelector[EditAccountError] {
    override def apply(error: EditAccountError): StatusCode = error match {
      case UserNotExistent(_) => BadRequest
      case BrandNotExistent(_) => BadRequest
      case BrandAlreadySubscribed(_) => BadRequest
      case InternalEditAccountError(_) => InternalServerError
    }
  }
}
