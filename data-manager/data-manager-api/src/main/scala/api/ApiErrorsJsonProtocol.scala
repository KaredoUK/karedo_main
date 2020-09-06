package api

import core.BrandActor.{InternalBrandError, InvalidBrandRequest, BrandError}
import core.EditAccountActor._
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

  implicit object brandErrorSelector extends ErrorSelector[BrandError] {
    def apply(error: BrandError): StatusCode = error match {
      case InvalidBrandRequest(reason) =>
        BadRequest
      case InternalBrandError(_) =>
        InternalServerError
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
