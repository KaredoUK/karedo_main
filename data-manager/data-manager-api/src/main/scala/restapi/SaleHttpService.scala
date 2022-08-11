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

object SaleHttpService {
  val logger = Logger("SaleService")
}

abstract class SaleHttpService(offerActor: ActorRef,
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
    pathPrefix("sale") {
      P116 ~ // Create Sale
      P117 ~ // Inquire Sale
      P118   // Complete Sale
  }
   
 def P116 : Route =
    path(JavaUUID / "create") { accountId =>

        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          post {
            handleWith {
              request: SaleCreate => (offerActor ? request).mapTo[ResponseWithFailure[OfferError,SaleResponse]]
            }
          }
        }
      }
  
def P117 : Route =
    path(JavaUUID) { saleId =>

        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          get {
            complete {
              (offerActor ? SaleRequestDetail(saleId)).mapTo[ResponseWithFailure[OfferError,SaleDetail]]
            }
          }
        }
      }
def P118 : Route =
    path("complete") {

        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          post {
            handleWith {
              request: SaleComplete => (offerActor ? request).mapTo[ResponseWithFailure[OfferError,SaleResponse]]
            }
          }
        }
      }
}
