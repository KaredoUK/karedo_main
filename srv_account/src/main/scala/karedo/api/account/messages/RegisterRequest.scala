package karedo.api.account.messages

import akka.Done
import play.api.libs.json.{Format, Json}

import scala.collection.immutable

case class RegisterRequest
(
  application_id: String,
  first_name: String,
  last_name: String,
  msisdn: String,
  user_type: String,
  email: String
) //extends Done

object RegisterRequest {
  implicit val format: Format[RegisterRequest] = Json.format[RegisterRequest]
}
