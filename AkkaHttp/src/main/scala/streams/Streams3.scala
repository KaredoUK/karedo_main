package streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{GraphDSL, _}
import GraphDSL.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object StreamProgram2 extends App {

  val sayFlow: Flow[String, String, NotUsed] =
    Flow[String].map { s =>
      s + "."
    }

  val shoutFlow: Flow[String, String, NotUsed] =
    Flow[String].map { s =>
      s + "!!!!"
    }
/*
  val sayAndShoutFlow: Flow[String, String, NotUsed] =
  GraphDSL.create() { implicit b =>

    val broadcast = b.add(Broadcast[String](2))
      val merge = b.add(Merge[String](2))

      broadcast ~> sayFlow ~> merge
      broadcast ~> shoutFlow ~> merge
      //(broadcast.in, merge.out)
    }*/

  implicit lazy val system = ActorSystem("example")
  implicit val materializer = ActorMaterializer()
  Source(List("Hello World"))
    //.via(sayAndShoutFlow)
    .runWith(Sink.foreach(println))
    .onComplete {
      case _ => system.terminate()
    }
}