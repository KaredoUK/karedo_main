package parallelai.wallet.offermanager.api

import scala.concurrent.ExecutionContext
import spray.routing.Directives
import akka.actor.ActorRef

/**
 * Created by crajah on 23/04/2014.
 */
class RetailOfferService(retailOfferActor: ActorRef) (implicit execContext: ExecutionContext)
  extends Directives with JSONSupport {

  val route =
    path("retail-offer") {
      get {
        handleWith {
          retailOfferGet: Any => (retailOfferActor ? retailOfferGet).mapTo[Either[_,_]]
        }
      }
    }
}
