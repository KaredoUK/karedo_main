package core.common

import core.{FailureResponse, ResponseWithFailure}

trait RequestValidationChaining {
  def withValidations[Request, Error, Response](request: Request)(validations: (Request => Option[Error])*)
                                               (successFlow: Request => ResponseWithFailure[Error, Response])
  : ResponseWithFailure[Error, Response] = {
    val validationResult = validations.foldLeft[Option[Error]](None) {
      (currStatus, currValidation) =>
        currStatus orElse currValidation(request)
    }

    validationResult  match {
      case Some(validationError) => FailureResponse(validationError)
      case None => successFlow(request)
    }
  }
}
