package karedo.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import karedo.actors.{APIResponse, Error}
import karedo.entity._
import karedo.util.{KO, OK, Result}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



/**
  * Created by pakkio on 05/10/16.
  */
trait KaredoRoute  {

  def doCall(f: => Result[Error, APIResponse]) =
      complete(
        Future {
          val result = f
          result match {
            case OK(response) =>
              HttpResponse(response.code,entity=HttpEntity(ContentTypes.`application/json`,response.msg))

            // @TODO: Response has to be as JSON as well. Format: { "error_code": "", "error_text": "" }
            case KO(Error(err,code)) =>
              HttpResponse(code,entity=HttpEntity(ContentTypes.`text/plain(UTF-8)`,err))

          }
        }
      )


}
