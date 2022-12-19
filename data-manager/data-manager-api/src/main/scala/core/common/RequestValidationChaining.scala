package core.common

import core.{FailureResponse, ResponseWithFailure}

trait RequestValidationChaining {
  def withValidations[Request, MyError, Response](request: Request)(validations: (Request => Option[MyError])*)
                                               (successFlow: Request => ResponseWithFailure[MyError, Response])
  : ResponseWithFailure[MyError, Response] = {
    val validationResult = validations.foldLeft[Option[MyError]](None) {
      (currStatus, currValidation) =>
        currStatus orElse currValidation(request)
    }

    validationResult  match {
      case Some(validationError) => FailureResponse(validationError)
      case None => successFlow(request)
    }
  }
}
