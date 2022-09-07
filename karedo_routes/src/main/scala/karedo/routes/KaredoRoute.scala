package karedo.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{APIResponse, Error}
import karedo.entity._
import karedo.util._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



/**
  * Created by pakkio on 05/10/16.
  */
trait KaredoRoute extends KaredoJsonHelpers with KaredoConstants  {

  val _log = LoggerFactory.getLogger(classOf[KaredoRoute])

  def route: Route

  def doCall(f: => Result[Error, APIResponse]) =
      complete(
        Future {
          val result = f
          result match {
            case OK(response) => {
              _log.debug(s"[CODE: ${response.code}] ${response.msg}")
              val entity = response.mime match {
                case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, response.msg)
                case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, response.msg)
                case MIME_JSON => HttpEntity(ContentTypes.`application/json`, response.msg)
                case _ => HttpEntity(ContentTypes.`application/json`, response.msg)
              }

              HttpResponse(response.code, entity = entity)
            }

            case KO(Error(err,code)) => {
              _log.error(s"[CODE: $code] $err")
              HttpResponse(code, entity = HttpEntity(ContentTypes.`application/json`,
                ErrorRes(code, None, err).toJson.toString
                ))
            }

          }
        }
      )
}
