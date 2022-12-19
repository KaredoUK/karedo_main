import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, _}
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration._

/**
  * Created by gerrit on 8/14/16.
  */
object WebService extends Directives {

  val host = "localhost"
  val port = 8080

  def questionRoutes: Route =  {

    path("question") {
      get {
        complete("answer")
      }
    } ~
    path("post") {

      post {
        handleWith {
          input: String =>
            s"answer to $input"
        }
      }
    }
  }

  implicit val system = ActorSystem("akka-http")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10 seconds)


  val api = questionRoutes
  Http().bindAndHandle(handler = api, interface = host, port = port) map { binding =>
    println(s"REST interface bound to ${binding.localAddress}")
  } recover { case ex =>
    println(s"REST interface could not bind to $host:$port", ex.getMessage)
  }

}