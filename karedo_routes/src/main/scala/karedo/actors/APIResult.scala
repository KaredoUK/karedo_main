package karedo.actors

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, _}
import akka.http.scaladsl.model.HttpHeader
import karedo.jwt.JWT

case class Error
(
  err: String,
  code: Int = 500,
  mime:String = "",
  headers:List[HttpHeader] = List()
) extends scala.Error

case class APIResponse
(
  msg: String,
  code: Int = 200,
  mime:String = "",
  headers:List[HttpHeader] = List(),
  bytes:Array[Byte] = Array(),
  jwt:Option[JWT] = None
)



