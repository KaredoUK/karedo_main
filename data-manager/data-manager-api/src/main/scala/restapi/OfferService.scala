package restapi

import akka.actor.ActorRef
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{OfferData, OfferResponse}
import core.OfferActor._
import core.ResponseWithFailure
import spray.routing.{Route, Directives}
import akka.pattern.ask

import scala.concurrent.ExecutionContext


object OfferService {
  val logger = Logger("OfferService")
}

class OfferService(offerActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route: Route =
    path("offer") {
      post {
        handleWith {
          offerData: OfferData =>
            (offerActor ? offerData).mapTo[ResponseWithFailure[OfferError, OfferResponse]]
        }
      }
    }
}
