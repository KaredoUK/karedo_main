package restapi

import akka.actor.ActorRef
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{OfferCode, OfferData, OfferResponse}
import core.OfferActor._
import core.ResponseWithFailure
import spray.routing.{Route, Directives}
import akka.pattern.ask

import restapi.security.AuthorizationSupport
import core.security.UserAuthService

import scala.concurrent.ExecutionContext


object OfferService {
  val logger = Logger("OfferService")
}

class OfferService(offerActor: ActorRef,
     override protected val userAuthService: UserAuthService)
    (implicit executionContext: ExecutionContext)
  extends Directives 
  with DefaultJsonFormats 
  with ApiErrorsJsonProtocol 
  with AuthorizationSupport {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route: Route =
    path("offer") {
      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        post {
          handleWith {
            offerData: OfferData =>
              (offerActor ? offerData).mapTo[ResponseWithFailure[OfferError, OfferResponse]]
          }
        }
      }
    } ~ P110

  lazy val P110 : Route =
    path("offer" / "validate") {

        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          post {
            handleWith {
              offer: OfferCode => (offerActor ? offer).mapTo[ResponseWithFailure[OfferError,OfferResponse]]
            }
          }
        }
      }

}
