package karedo.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import karedo.entity.dao.{KO, OK, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by pakkio on 05/10/16.
  */
trait KaredoRoute {
  case class Error(err: String)

  case class APIResponse(msg: String, code: Int = 200)

  def doCall(f: => Result[Error, APIResponse]) =
    respondWithHeader(RawHeader("Content-Type", "application/json")) {
      complete(
        Future {
          val result = f
          result match {

            case OK(APIResponse(msg, code)) =>
              StatusCode.int2StatusCode(code) -> msg
            case KO(Error(err)) => (StatusCodes.InternalServerError -> err)

          }
        }
      )
    }

}
