package restapi

import akka.actor.ActorRef
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data._
import core.OfferActor._
import core.ResponseWithFailure
import spray.routing.{Route, Directives}
import akka.pattern.ask
import restapi.security.AuthorizationSupport
import core.security.UserAuthService
import scala.concurrent.ExecutionContext
import spray.routing.HttpService

object OfferHttpService {
  val logger = Logger("OfferService")
}

abstract class OfferHttpService(offerActor: ActorRef,
     override protected val userAuthService: UserAuthService)
    (implicit executionContext: ExecutionContext)
  extends HttpService 
  with Directives 
  with DefaultJsonFormats 
  with ApiErrorsJsonProtocol 
  with AuthorizationSupport {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route: Route =
    pathPrefix("offer"){
      P110 ~ // getOfferCode
      P111 ~ // validateCode
      P112   // consumeCode
    }
  
   
  def P110: Route =
  // PARALLELAI-110API: User Offer Code
  // POST "offer/userid/getcode"
  path(JavaUUID / "getcode"){
    (userId) => {
      userAuthorizedFor(canAccessUser(userId))(executionContext){ userAuthContext =>
        post {

          handleWith { getOfferCode : GetOfferCodeRequest =>
            (offerActor ? getOfferCode)
              .mapTo[ResponseWithFailure[OfferError,GetOfferCodeResponse]]
          }
        }
      }
    }
  }
  def P111 : Route =
    path("validate") {

        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          post {
            handleWith {
              offer: OfferCode => (offerActor ? OfferValidate(offer.offerCode)).mapTo[ResponseWithFailure[OfferError,OfferResponse]]
            }
          }
        }
      }
 def P112 : Route =
    path("consume") {

        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          post {
            handleWith {
              offer: OfferCode => (offerActor ? OfferConsume(offer.offerCode)).mapTo[ResponseWithFailure[OfferError,OfferResponse]]
            }
          }
        }
      }
 
}
