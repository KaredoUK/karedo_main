package karedo.api.account.messages

import akka.Done
import play.api.libs.json.{Format, Json}

sealed trait AccountCommand[R] //extends ReplyType[R]

case class RegisterMessage(message: RegisterRequest) //extends ReplyType[Done]

object RegisterMessage {
  implicit val format: Format[RegisterMessage] = Json.format[RegisterMessage]

}