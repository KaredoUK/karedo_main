package core

sealed trait ResponseWithFailure[+FailureType, +SuccessType]
final case class SuccessResponse[+FailureType, +SuccessType](val content: SuccessType) extends ResponseWithFailure[FailureType, SuccessType]
final case class FailureResponse[+FailureType, +SuccessType](val failure: FailureType) extends ResponseWithFailure[FailureType, SuccessType]