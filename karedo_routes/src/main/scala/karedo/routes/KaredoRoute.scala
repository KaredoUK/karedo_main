package karedo.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.headers._
import karedo.actors.{APIResponse, Error}
import karedo.entity._
import karedo.util._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import spray.json._

import scala.collection.mutable
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
            case OK(APIResponse(msg,code, mime, headers, bytes)) => {
              _log.debug(s"[CODE: ${code}] ${msg}")
              val entity = mime match {
                case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, msg)
                case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, msg)
                case MIME_JSON => HttpEntity(ContentTypes.`application/json`, msg)
                case MIME_PNG => HttpEntity(MediaTypes.`image/png`, bytes)
                case _ => HttpEntity(ContentTypes.`application/json`, msg)
              }

              HttpResponse(code, entity = entity, headers = headers)
            }

            case KO(Error(err,code, mime, headers)) => {
              _log.error(s"[CODE: $code] $err")
              val entity = mime match {
                case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, err)
                case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, err)
                case MIME_JSON => HttpEntity(ContentTypes.`application/json`, ErrorRes(code, None, err).toJson.toString)
                case _ => HttpEntity(ContentTypes.`application/json`, ErrorRes(code, None, err).toJson.toString)
              }

              HttpResponse(code, entity = entity, headers = headers)
            }

          }
        }
      )
}
