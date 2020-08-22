package core.common

import core.{FailureResponse, ResponseWithFailure}

trait RequestValidationChaining {
  def withValidations[Request, Error, Response](request: Request)(validations: (Request => Option[Error])*)
                                               (successFlow: Request => ResponseWithFailure[Error, Response])
  : ResponseWithFailure[Error, Response] = {
    val validationResult = validations.foldLeft[Option[Error]](None) {
      case (currStatus, currFunction) =>
        currStatus flatMap { _ match {
          case Some(_) => currStatus
          case None => currFunction(request)
        }
        }
    }
    validationResult  match {
      case Some(validationError) => FailureResponse(validationError)
      case None => successFlow(request)
    }
  }
}
