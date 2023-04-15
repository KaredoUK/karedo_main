package karedo.route.common

sealed trait ResponseWithFailure[+FailureType, +SuccessType] {
  def map[T] (f: SuccessType => T): ResponseWithFailure[FailureType, T]
}
final case class SuccessResponse[+FailureType, +SuccessType](val content: SuccessType) extends ResponseWithFailure[FailureType, SuccessType] {
  def map[T] (f: SuccessType => T): ResponseWithFailure[FailureType, T] = SuccessResponse( f(content) )
}

final case class FailureResponse[+FailureType, +SuccessType](val failure: FailureType) extends ResponseWithFailure[FailureType, SuccessType] {
  def map[T] (f: SuccessType => T): ResponseWithFailure[FailureType, T] = FailureResponse[FailureType, T](failure)
}

