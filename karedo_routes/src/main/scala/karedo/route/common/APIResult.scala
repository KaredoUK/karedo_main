package karedo.route.common

import spray.json.{JsObject, JsString, RootJsonWriter}

object objAPI {


  trait APIError

  case class InvalidRequest(reason: String) extends APIError
  case class InternalError(reason: Throwable=null,msg:String="") extends APIError

  //trait APIOK

  case class APIOK(msg: String, code:Int = 200)

  implicit object errorJsonFormat extends RootJsonWriter[APIError] {
    def write(error: APIError) = error match {
      case InvalidRequest(reason) => JsObject(
        "type" -> JsString("InvalidRequest"),
        "data" -> JsObject {
          "reason" -> JsString(reason)
        }
      )
      case InternalError(reason,msg) => JsObject(
        "type" -> JsString("InternalError"),
        "data" -> JsObject (
          "reason" -> JsString(reason.getMessage),
          "message" -> JsString(msg)
        )
      )
    }


  }

}
