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

//import spray.http.Uri.Path

import scala.concurrent.Future

// All APIs starting with /account go here
@Api(position = 1, value = "/account", description = "Operations on the account")
abstract class AccountHttpService2


  extends HttpService
    with Directives
    with DefaultJsonFormats
    with AccountAds
    with ApiErrorsJsonProtocol
    with ApiDataJsonProtocol
    with CORSDirectives {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  def route =
    pathPrefix("account") {
      getAds ~ getInfoOptions ~ postAccount

    }

  @Path("/getads")
  @ApiOperation(position = 1, httpMethod = "GET", response = classOf[UserProfileExt], value = "KAR-200: GetInfo2")
  @ApiImplicitParams(Array())
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")))
  def getAds = corsFilter(List("*")) {
    path("getads") {
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

  def postAccount = {
    path("getads") {
      post {
        entity(as[AccountGetadsRequest]) { data =>
          complete(AccountGetadsResponse(data.accountId, data.deviceId, data.sessionId, List("ad1", "ad2")))
        }
      }
    }
  }

  @Path("/getInfo")
  @ApiOperation(position = 1, httpMethod = "OPTIONS", response = classOf[String], value = "KAR-200: GetInfo2")
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
