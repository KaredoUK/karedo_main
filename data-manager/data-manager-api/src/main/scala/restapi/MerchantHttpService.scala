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
import com.wordnik.swagger.annotations._
import javax.ws.rs.Path
import spray.routing.HttpService

object MerchantHttpService {
  val logger = Logger("MerchantService")
}

@Api(value = "/merchant", description = "merchant utilities", position = 4)
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
      P119 ~ P119a  // Get karedos change
    }

  @Path("/karedos/{currency}")
  @ApiOperation(position=1,httpMethod = "GET", response = classOf[KaredoChange],
    value = "Parallelai-119: change for that currency")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "currency", required = true,
      dataType = "String", paramType = "path",
      value = "currency for which to get the change for 1 karedo"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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

  @Path("/karedos/{currency}")
  @ApiOperation(position=1,httpMethod = "POST", response = classOf[KaredoChange],
    value = "Parallelai-119a: set change for a currency")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "currency", required = true,
      dataType = "String", paramType = "path",
      value = "currency for which to get the change for 1 karedo"),
    new ApiImplicitParam(
      name = "Change Data",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.KaredoChange",
      paramType = "body",
      value = "Currency and Change value"),  
      
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
