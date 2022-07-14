package restapi

import javax.ws.rs.Path

import com.wordnik.swagger.annotations._
import core.OfferActor.OfferError
import core.objAPI.APIError
import parallelai.wallet.entity.KaredoSales
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data._
import core.EditAccountActor.EditAccountError
import core.security.UserAuthService
import spray.routing._

import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.pattern.ask
import core.{ResponseWithFailure, SuccessResponse}
import akka.util.Timeout
import core.RegistrationActor._
import core.EditAccountActor._
import java.util.UUID

import spray.http.StatusCodes

import scala.concurrent.Future

// All APIs starting with /account go here
@Api(position = 1, value = "/account", description = "Operations on the account")
abstract class AccountHttpService2(
  /*protected val registrationActor: ActorRef,
  protected val editAccountActor: ActorRef,
  protected val brandActor: ActorRef,
  protected val offerActor: ActorRef,*/
  override protected val userAuthService: UserAuthService)(protected implicit val executionContext: ExecutionContext)

    extends HttpService
    with Directives
    with DefaultJsonFormats
    with ApiErrorsJsonProtocol
    with ApiDataJsonProtocol
    with CORSDirectives
    with AuthorizationSupport {

  import scala.concurrent.duration._
  implicit val timeout = Timeout(20.seconds)

  def route =
    pathPrefix("account") {
      getInfo ~ getInfoOptions

    }

  @Path("/getInfo")
  @ApiOperation(position = 1, httpMethod = "GET", response = classOf[UserProfileExt], value = "KAR-200: GetInfo2")
  @ApiImplicitParams(Array())
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")))
  def getInfo = corsFilter(List("*")) {
    path("getInfo") {
      get {
        complete(UserProfileExt(
          UserInfo(userType = "USER", fullName = "John Doe"),
          Intent(want = "buy"),
          Preferences(List("IAB1", "IAB2")),
          UserSettings(2),
          770));
      }
    }
  }
  
   @Path("/getInfo")
  @ApiOperation(position = 1, httpMethod = "OPTIONS", value = "KAR-200: GetInfo2")
  @ApiImplicitParams(Array())
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")))
  def getInfoOptions = corsFilter(List("*")) {
    path("getInfo") {
      options {
        complete(StatusCodes.Accepted -> "OK")
      }
    }
  }

}
