package karedo.route.routes

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error}
import karedo.common.jwt.JWTWithKey
import karedo.route.common.{KaredoConstants, KaredoJsonHelpers}
import karedo.route.util._
import org.slf4j.LoggerFactory
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import karedo.common.result.{Result, OK, KO}

/**
  * Created by pakkio on 05/10/16.
  */
trait KaredoRoute extends KaredoJsonHelpers with KaredoConstants with JWTWithKey  {

  val _log = LoggerFactory.getLogger(classOf[KaredoRoute])

  def route: Route

  def doCall(f: => Result[Error, APIResponse]) =
      complete(
        Future {
          val result = f
          result match {
            case OK(APIResponse(msg,code, mime, headers, bytes, jwt)) => {
              _log.debug(s"[CODE: ${code}] ${msg}")
              val entity = mime match {
                case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, msg)
                case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, msg)
                case MIME_JSON => HttpEntity(ContentTypes.`application/json`, msg)
                case MIME_PNG => HttpEntity(MediaTypes.`image/png`, bytes)
                case _ => HttpEntity(ContentTypes.`application/json`, msg)
              }

              val new_headers = jwt match {
                case Some(j) => {
                  val token = getJWTToken(j)
                  headers ++ List(RawHeader("X-Authorization", s"TOKEN ${token}"))
                }
                case None => headers
              }

              HttpResponse(code, entity = entity, headers = new_headers)
            }

            case KO(Error(err,code, mime, headers)) => {
              _log.error(s"[CODE: $code] $err")
              val entity = mime match {
                case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, err)
                case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, err)
                case MIME_JSON => HttpEntity(ContentTypes.`application/json`, ErrorRes(code, None, err).toJson.prettyPrint)
                case _ => HttpEntity(ContentTypes.`application/json`, ErrorRes(code, None, err).toJson.prettyPrint)
              }

              HttpResponse(code, entity = entity, headers = headers)
            }

          }
        }
      )

  def doJWTCall(f: => Future[Try[APIResponse]]) = {
    complete (
      f map { result =>
        result match {
          case Success(APIResponse(msg,code, mime, headers, bytes, jwt)) => {
            _log.debug(s"[CODE: ${code}] ${msg}")
            val entity = mime match {
              case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, msg)
              case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, msg)
              case MIME_JSON => HttpEntity(ContentTypes.`application/json`, msg)
              case MIME_PNG => HttpEntity(MediaTypes.`image/png`, bytes)
              case _ => HttpEntity(ContentTypes.`application/json`, msg)
            }

            val new_headers = jwt match {
              case Some(j) => {
                val token = getJWTToken(j)
                headers ++ List(RawHeader("X-Authorization", s"TOKEN ${token}"))
              }
              case None => headers
            }

            HttpResponse(code, entity = entity, headers = new_headers)
          }

          case Failure(Error(err,code, mime, headers)) => {
            _log.error(s"[CODE: $code] $err")
            val entity = mime match {
              case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, err)
              case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, err)
              case MIME_JSON => HttpEntity(ContentTypes.`application/json`, ErrorRes(code, None, err).toJson.prettyPrint)
              case _ => HttpEntity(ContentTypes.`application/json`, ErrorRes(code, None, err).toJson.prettyPrint)
            }

            HttpResponse(code, entity = entity, headers = headers)
          }

          case _ => {
            val entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "")

            HttpResponse(StatusCodes.InternalServerError, entity = entity, headers = Nil)
          }
        }
      }
    )
  }
}
