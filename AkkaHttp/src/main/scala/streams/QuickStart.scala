package streams

import akka.stream._
import akka.stream.scaladsl._

import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

import scala.concurrent.ExecutionContext.Implicits.global

object QuickStart extends App {

  val source: Source[Int, NotUsed] = Source(1 to 100)

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  source.runForeach(i => println(i))(materializer)

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  val result: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  result onSuccess { case err => println(err) }

  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  factorials.map(_.toString).runWith(lineSink("factorial2.txt"))

  val done: Future[Done] =
    factorials
    .zipWith(Source(0 to 100))((num,idx)=> s"$idx! = $num")
    .throttle(1,1.second, 1, ThrottleMode.shaping)
    .runForeach(println)

}
