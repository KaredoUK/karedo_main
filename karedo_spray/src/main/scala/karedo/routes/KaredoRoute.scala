package karedo.routes

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import karedo.entity.dao.{KO, OK, Result}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pakkio on 05/10/16.
  */
trait KaredoRoute {
  case class Error(err: String)

  case class APIResponse(msg: String, code: Int = 200)

  def doCall(f: => Result[Error, APIResponse]) =
    complete(
      Future {
        val result = f
        result match {

          case OK(APIResponse(msg, code)) => (StatusCode.int2StatusCode(code) -> msg)
          case KO(Error(err)) => (StatusCodes.InternalServerError -> err)

        }
      }
    )

}
