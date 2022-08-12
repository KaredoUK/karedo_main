package streams

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future

final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t) }.toSet
}

object ReactiveTweets extends App {
  val akka = Hashtag("#akka")
  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()

  val tweets: Source[Tweet, NotUsed] = Source(
    List(
      Tweet(Author("pippo"), 0, "#akka is #good"),
      Tweet(Author("pluto"), 1, "here #again #akka"),
      Tweet(Author("minnie"), 2, "#not #hashtag")
    ))

  val authors: Source[Author, NotUsed] =
    tweets
      .filter(_.hashtags.contains(akka))
      .map(_.author)

  def title(s: String) = println(s"\n$s\n==============")

  title("here is authors")
  authors.runWith(Sink.foreach(println))

  Thread.sleep(1000)
  val hashtags: Source[Hashtag, NotUsed] = tweets.mapConcat(_.hashtags.toList)
  title("here is hashtags")
  hashtags.runWith(Sink.foreach(println))

  Thread.sleep(1000)

  title("here is the graph in action")
  val writeAuthors: Sink[Author, Future[Done]] = Sink.foreach(x => println(s"author:$x"))
  val writeHashtags: Sink[Hashtag, Future[Done]] = Sink.foreach(x => println(s"hashtag:$x"))
  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
    val bcast = b.add(Broadcast[Tweet](2))
    tweets ~> bcast.in
    bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
    bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
    ClosedShape
  })
  g.run()
  Thread.sleep(1000)
  title("counting sum of source")

  val count: Flow[Tweet, Int, NotUsed] =
    Flow[Tweet].map(_ => 1)

  val sumSink: Sink[Int, Future[Int]] =
    Sink.fold[Int, Int](0)(_ + _)

  val counterGraph: RunnableGraph[Future[Int]] =
    tweets
      .via(count)
      .toMat(sumSink)(Keep.right)

  import scala.concurrent.ExecutionContext.Implicits.global

  val sum: Future[Int] = counterGraph.run()
  sum.foreach(c => println(s"Total tweets processed: $c"))
  Thread.sleep(1000)

  title("other example")

  val counterRunnableGraph: RunnableGraph[Future[Int]] =
    tweets
      .filter(_.hashtags contains akka)
      .map(t => 1)
      .toMat(sumSink)(Keep.right)

  val morningTweetsCount: Future[Int] = counterRunnableGraph.run()

  morningTweetsCount.foreach(c => println(s"total now is $c"))

  Thread.sleep(1000)
  title("last example")
  val sum2: Future[Int] =
    tweets.map(t => 1).runWith(sumSink)

  sum2.foreach(c => println(s"runwith returning $c"))


}
