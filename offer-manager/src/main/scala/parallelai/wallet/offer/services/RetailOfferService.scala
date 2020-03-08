package parallelai.wallet.offer.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.routing.Directives
import api.DefaultJsonFormats

/**
 * Created by crajah on 27/04/2014.
 */
class RetailOfferService (registration: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  val route =
    path("offer") {
      get {
        handleWith {

        }
      }
    }

}
