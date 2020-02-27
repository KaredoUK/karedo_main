package core

import akka.actor.{Props, ActorSystem}
import scala.annotation.tailrec
import spray.routing.HttpService
import spray.routing.authentication.BasicAuth
import spray.routing.directives.CachingDirectives
import scala.concurrent.duration.Duration
import spray.httpx.encoding.Deflate
import spray.routing.SimpleRoutingApp

object Main extends App with SimpleRoutingApp {
  import Commands._

  implicit val system = ActorSystem()
//  val sentiment = system.actorOf(Props(new SentimentAnalysisActor with CSVLoadedSentimentSets with AnsiConsoleSentimentOutput))
//  val stream = system.actorOf(Props(new TweetStreamerActor(TweetStreamerActor.twitterUri, sentiment) with OAuthTwitterAuthorization))


  /*
  @tailrec
  private def commandLoop(): Unit = {
    Console.readLine() match {
      case QuitCommand         => return
      case TrackCommand(query) => stream ! query
      case _                   => println("WTF??!!")
    }

    commandLoop()
  }

  // start processing the commands
  commandLoop()
  */

  startServer(interface = "localhost", port = 8080) {
    path("offers") {
      get {
        complete {
          <h1>Offer List</h1>
        }
      }
    }
  }
}

object Commands {

  val QuitCommand   = "quit"
  val TrackCommand = "track (.*)".r

}

/*
trait OfferManagerService extends HttpService {

//  val simpleCache = CachingDirectives.routeCache(maxCapacity = 1000, timeToIdle = Duration("15 min"))

  val route = {
    path("offer") {
      user => get {
        encodeResponse(Deflate) {
          complete {
            "Offer List"
          }
        }
      }
    }
  }

}
*/
