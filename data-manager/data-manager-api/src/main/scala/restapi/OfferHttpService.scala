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

object OfferHttpService {
  val logger = Logger("OfferService")
}

@Api(value = "/offer", description = "Offers workflow", position = 3)
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
  
   
  @Path("/{userId}/getcode")
  @ApiOperation(position=1,httpMethod = "POST", response = classOf[GetOfferCodeResponse], 
      value = "Parallelai-110: getoffer code (creating an offer in sale collection)")
  @ApiImplicitParams(Array(
     new ApiImplicitParam(name = "userId", required = true,
      dataType = "String", paramType = "path",
      value = "user requesting code (creating the offer)"),
    new ApiImplicitParam(name = "request", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.GetOfferCodeRequest", paramType = "body",
      value = "Code to be validated"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
  @Path("/validate")
  @ApiOperation(position=2,httpMethod = "POST", response = classOf[OfferResponse], value = "Parallelai-111: check if an offer code is valid")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "request", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.OfferCode", paramType = "body",
      value = "Code to be validated"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
    @Path("/consume")
  @ApiOperation(position=3,httpMethod = "POST", response = classOf[OfferResponse], value = "Parallelai-112: consume an offer")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "request", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.OfferCode", paramType = "body",
      value = "Code to be validated"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
