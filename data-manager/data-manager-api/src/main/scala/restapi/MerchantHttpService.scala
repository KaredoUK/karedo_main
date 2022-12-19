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

object MerchantHttpService {
  val logger = Logger("MerchantService")
}

abstract class MerchantHttpService(offerActor: ActorRef,
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
    pathPrefix("merchant") {
      P119 ~ P119a ~ P115  // Get karedos change
    }

  def P119 : Route =
    path("karedos" / Segment ) { currency : String =>

      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        get {
          complete {
            (offerActor ? RequestKaredoChange(currency)).mapTo[ResponseWithFailure[OfferError,KaredoChange]]
          }
        }
      }
    }
  
  def P115 : Route =
    path("convertmoney" ) { 

      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        put {
          handleWith { currency: Currency  =>
            (offerActor ? currency).mapTo[ResponseWithFailure[OfferError,Currency]]
          }
        }
      }
    }

  def P119a : Route =
    path("karedos" / Segment ) { currency : String =>

      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        post {
          handleWith { request: KaredoChange =>
            (offerActor ? request).mapTo[ResponseWithFailure[OfferError,KaredoChange]]
          }
        }
      }
    }


}
