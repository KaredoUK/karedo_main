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

object SaleHttpService {
  val logger = Logger("SaleService")
}

@Api(value = "/sale", description = "merchant sales", position = 4)
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
      P116 ~ 
      P117 ~ 
      P118
  }
   
  @Path("/{merchantId}/create")
  @ApiOperation(httpMethod = "POST", response = classOf[OfferResponse], value = "Parallelai-116: check if a code is valid")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "merchantId", required = true,
      dataType = "String", paramType = "path",
      value = "merchant creating this sale"),
    new ApiImplicitParam(name = "", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.SaleCreate", paramType = "body",
      value = "Sale to be created"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
  
  @Path("/{saleId}")
  @ApiOperation(httpMethod = "GET", response = classOf[OfferResponse], value = "Parallelai-117: check if a code is valid")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "saleId", required = true,
      dataType = "String", paramType = "path",
      value = "SaleId to be read"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
  @Path("/complete")
  @ApiOperation(httpMethod = "POST", response = classOf[OfferResponse], value = "Parallelai-118: complete a sale")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "merchantId", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.OfferCode", paramType = "body",
      value = "merchant creating this sale"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
