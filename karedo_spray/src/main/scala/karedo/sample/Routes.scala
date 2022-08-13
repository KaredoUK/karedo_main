package karedo.sample

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow




/**
  * Created by gerrit on 8/15/16.
  */
object Routes extends Entities  {

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
  }


  val route: Route = Route(

    path("hello") {
      get( complete("World"))
    } ~

    path("get" / Segment ) { p:String =>
      get(
        complete( HttpEntity(ContentTypes.`text/html(UTF-8)`,s"<h1>Hello get $p</h1>"))
      )


    } ~
    path("post"){
      post {
        handleWith {
          s:String => HttpEntity(ContentTypes.`text/html(UTF-8)`,s"<h1>Hello post $s</h1>")
        }
      }
    } ~
    path("json"){
      post {
        entity(as[Record]) {
          s => complete(Record(s.a+1,s.b+"!"))
        }
      }
    }
  )
}
