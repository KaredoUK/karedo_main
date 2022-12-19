package streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent.ExecutionContext.Implicits.global

object StreamProgram extends App {

    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    Source(List("Hello World"))
      .map((s: String) => s + "!")
      .runWith(Sink.foreach(println))
      .onComplete {
        case _ => system.terminate()
      }
}
