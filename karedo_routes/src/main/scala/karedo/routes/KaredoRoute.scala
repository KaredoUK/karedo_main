package karedo.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import karedo.entity._
import karedo.entity.dao.{KO, OK, Result}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pakkio on 05/10/16.
  */
trait KaredoRoute  extends DefaultJsonProtocol with SprayJsonSupport {
  case class Error(err: String)



  case class APIResponse(msg: String, code: Int = 200)

  def doCall(f: => Result[Error, APIResponse]) =
      complete(
        Future {
          val result = f
          result match {
            case OK(response) =>
              HttpResponse(response.code,entity=HttpEntity(ContentTypes.`application/json`,response.msg))
            case KO(Error(err)) =>
              HttpResponse(400,entity=HttpEntity(ContentTypes.`text/plain(UTF-8)`,err))

          }
        }
      )

  val dbUserApp = new DbUserApp {}
  val dbUserAccount = new DbUserAccount {}
  val dbUserSession = new DbUserSession {}
  val dbUserAd = new DbUserAd {}
  //case class Ad(url:String)
  //case class AdsReturned(List[Ad]=List())

  implicit val jsonChannel = jsonFormat2(Channel)
  implicit val jsonUserAd = jsonFormat10(UserAd)

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

    val formatter = ISODateTimeFormat.basicDateTimeNoMillis

    def write(obj: DateTime): JsValue = {
      JsString(formatter.print(obj))
    }

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => try {
        formatter.parseDateTime(s)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }
}
