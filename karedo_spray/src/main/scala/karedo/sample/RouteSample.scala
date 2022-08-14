package karedo.sample

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

/**
  * Created by pakkio on 10/3/16.
  */
trait RouteSample {
  val routeSample: Route = Route(


    path("hello") {
      get(complete(Future {
        "World"
      }))
    } ~

      path("get" / Segment) { p: String =>
        get(
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Hello get $p</h1>"))
        )


      } ~
      path("post") {
        post {
          handleWith {
            s: String => HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Hello post $s</h1>")
          }
        }
      } ~
      path("json") {
        post {
          entity(as[Record]) {
            s => complete(Record(s.a + 1, s.b + "!"))
          }
        }
      }
  )
}
