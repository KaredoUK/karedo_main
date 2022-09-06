package karedo.actors

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, _}

case class Error(err: String, code: Int = 500) extends scala.Error
case class APIResponse(msg: String, code: Int = 200)



