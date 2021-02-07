package restapi

import javax.ws.rs.Path

import com.wordnik.swagger.annotations._
import core.OfferActor.OfferError
import core.objAPI.APIError
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data._

import core.EditAccountActor.EditAccountError
import core.security.UserAuthService
import spray.routing._
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.pattern.ask
import core.{SuccessResponse, ResponseWithFailure}
import akka.util.Timeout
import core.RegistrationActor._
import core.EditAccountActor._
import java.util.UUID

// All APIs starting with /account go here
@Api(value = "/user", description = "user interactions.", position = 5)
abstract class UserHttpService
(
  protected val registrationActor: ActorRef,
  protected val editAccountActor: ActorRef,
  protected val brandActor: ActorRef,
  protected val offerActor: ActorRef,
  override protected val userAuthService: UserAuthService)
(protected implicit val executionContext: ExecutionContext)

extends HttpService
  with Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with ApiDataJsonProtocol
  with AuthorizationSupport {

  import scala.concurrent.duration._
  implicit val timeout = Timeout(20.seconds)

  def route =
    pathPrefix("user") {
      userBrandInteraction ~ // P108 POST AUTH /user/xxx/interaction/brand
      userOfferInteraction ~  // P107 POST AUTH /user/xxx/interaction/offer
      userOfferCode           // P110 get offer code /user/xxx/getcode
    }

   @Path("/{account}/interaction/brand")
  @ApiOperation(httpMethod = "GET", response = classOf[InteractionResponse],
    value = "Parallelai-55: User interacting with brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user interacting"),
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand"),
    new ApiImplicitParam(name = "intType", required = true, dataType = "String", paramType = "body",
      value = "interactionType"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
          value = "SessionId for authentication/authorization")
  ))
  def userBrandInteraction: Route =

  // PARALLELAI-55API: User Brand Interaction
  // PARALLELAI-108API:
  // "user/"+userId+"/interaction/brand/"
    path(JavaUUID / "interaction" / "brand") {
      (user) => {
        userAuthorizedFor(canAccessUser(user))(executionContext) { userAuthContext =>
          post {
            handleWith((interaction: UserBrandInteraction) =>

              (brandActor ? interaction)
                .mapTo[ResponseWithFailure[APIError, InteractionResponse]]
              // s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}")
            )
          }
        }
      }

    }

  def userOfferInteraction: Route =

  // PARALLELAI-107API: User Offer Interaction
  // "user/"+userId+"/interaction/offer"
    path(JavaUUID / "interaction" / "offer") {
      (user) => {
        userAuthorizedFor(canAccessUser(user))(executionContext) { userAuthContext =>
          post {
            handleWith((interaction: UserOfferInteraction) =>

              (brandActor ? interaction)
                .mapTo[ResponseWithFailure[APIError, InteractionResponse]]
              // s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}")
            )
          }
        }
      }

    }

  def userOfferCode: Route =
  // PARALLELAI-110API: User Offer Code
  // POST "user/xxx/offer/yyyy/getcode"
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

}
