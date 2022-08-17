package karedo.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import karedo.entity.dao.{KO, OK, Result}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
            case OK(response) =>
              HttpResponse(response.code,entity=HttpEntity(ContentTypes.`application/json`,response.msg))
            case KO(Error(err)) =>
              HttpResponse(400,entity=HttpEntity(ContentTypes.`text/plain(UTF-8)`,err))

          }
        }
      )
}
