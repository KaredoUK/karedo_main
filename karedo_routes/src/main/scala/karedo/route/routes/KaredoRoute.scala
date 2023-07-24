package karedo.route.routes

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error}
import karedo.common.jwt.{JWT, JWTWithKey}
import karedo.route.common.{KaredoConstants, KaredoJsonHelpers}
import org.slf4j.LoggerFactory
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}

/**
  * Created by pakkio on 05/10/16.
  */
trait KaredoRoute extends KaredoJsonHelpers with KaredoConstants with JWTWithKey  {

  val _log = LoggerFactory.getLogger(classOf[KaredoRoute])

  def route: Route

  private def addJWTHeader(jwt: Option[JWT], headers: List[HttpHeader]) = {
    jwt match {
      case Some(j) => {
        val token = getJWTToken(j)
        headers ++ List(RawHeader(AUTH_HEADER_NAME, s"TOKEN ${token}"))
      }
      case None => headers
    }
  }

  private def getSuccessResponse(response: APIResponse): HttpResponse = {
    _log.debug(s"[CODE: ${response.code}] ${response.msg}")
    val entity = response.mime match {
      case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, response.msg)
      case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, response.msg)
      case MIME_JSON => HttpEntity(ContentTypes.`application/json`, response.msg)
      case MIME_PNG => HttpEntity(MediaTypes.`image/png`, response.bytes)
      case _ => HttpEntity(ContentTypes.`application/json`, response.msg)
    }

    val new_headers = addJWTHeader(response.jwt, response.headers)

    HttpResponse(response.code, entity = entity, headers = new_headers)
  }

  private def getFailiureResponse(error: Error): HttpResponse = {
    _log.error(s"[CODE: ${error.code}] ${error.err}")
    val entity = error.mime match {
      case MIME_TEXT => HttpEntity(ContentTypes.`text/plain(UTF-8)`, error.err)
      case MIME_HTML => HttpEntity(ContentTypes.`text/html(UTF-8)`, error.err)
      case MIME_JSON => HttpEntity(ContentTypes.`application/json`, ErrorRes(error.code, None, error.err).toJson.prettyPrint)
      case _ => HttpEntity(ContentTypes.`application/json`, ErrorRes(error.code, None, error.err).toJson.prettyPrint)
    }

    HttpResponse(error.code, entity = entity, headers = error.headers)
  }

  def doCallOld(f: => Result[Error, APIResponse]) =
      complete(
        Future {
          val result = f
          result match {
            case OK(response) => getSuccessResponse(response)
            case KO(error) => getFailiureResponse(error)
          }
        }
      )

  def doCall(f: => Result[Error, APIResponse]) = {
    complete {
      resultToFuture(f).transformWith {
        case Success(response) => Future(getSuccessResponse(response))
        case Failure(error: Error) => Future(getFailiureResponse(error))
      }
    }
  }

  private def resultToFuture(f: => Result[Error, APIResponse]): Future[APIResponse] = {
    Future.fromTry(f.fold(e => Failure(e), s => Success(s)))
  }

  def doJWTCall(f: => Future[APIResponse]) = {
    complete {
      f.transformWith {
        case Success(response) => Future(getSuccessResponse(response))
        case Failure(error: Error) => Future(getFailiureResponse(error))
      }
    }
  }
}
