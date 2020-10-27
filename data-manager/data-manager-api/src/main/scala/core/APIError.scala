package core

import spray.json.{JsString, JsObject, RootJsonWriter}
import api.ApiErrorsJsonProtocol

/**
 * Created by pakkio on 09/12/2014.
 * APIErrors handles generic errors like invalidRequest or internalError
 */



object objAPI {


  trait APIError

  case class InvalidRequest(reason: String) extends APIError
  case class InternalError(reason: Throwable) extends APIError

  implicit object errorJsonFormat extends RootJsonWriter[APIError] {
    def write(error: APIError) = error match {
      case InvalidRequest(reason) => JsObject(
        "type" -> JsString("InvalidRequest"),
        "data" -> JsObject {
          "reason" -> JsString(reason)
        }
      )
      case InternalError(reason) => JsObject(
        "type" -> JsString("InternalError"),
        "data" -> JsObject {
          "reason" -> JsString(reason.getMessage)
        }
      )
    }


  }

}
