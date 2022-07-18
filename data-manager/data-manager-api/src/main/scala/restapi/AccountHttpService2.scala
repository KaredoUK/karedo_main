package restapi

import javax.ws.rs.Path

import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import com.wordnik.swagger.annotations.{ApiImplicitParams, ApiOperation, ApiResponses, _}
import spray.http.StatusCodes
import spray.routing._

//import spray.http.Uri.Path

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
      myOptions ~ postAccount

    }

  @Path("/{account}/suggestedOffers")
  @ApiOperation(position=1,httpMethod = "POST", response = classOf[AccountSuggestedOffersResponse],
    value = "KAR-126: account deviceId, sessionId, accountId [PROTOTYPE]")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user account"),
    new ApiImplicitParam(
      name = "ask for requested offers",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.AccountSuggestedOffersRequest",
      paramType = "body",
      value = "identification parameters")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
  def postAccount = corsFilter(List("*")){
    path(Segment / "suggestedOffers") {

      accountId: String =>
        post {
          entity(as[AccountSuggestedOffersRequest]) {
            data: AccountSuggestedOffersRequest =>
              if (accountId == "0") {
                if (data.sessionId == "") {
                  // KAR-126/1 ACCOUNT0 - DEVID - SESSION EMPTY RETURNS A SESSION
                  complete(AccountSuggestedOffersResponse(fixedSessionId))
                }
                else {
                  // KAR-126/2 ACCOUNT0 - DEVID - SESSION FULL
                  if (data.sessionId != fixedSessionId && data.sessionId != fixedSessionId2) {
                    complete(StatusCodes.Forbidden) // wrong session id
                  } else {
                    if (data.deviceId != fixedDevIdMd5) {
                      complete(StatusCodes.BadRequest) // wrong device id
                    } else {
                      complete(AccountSuggestedOffersResponse(fixedSessionId2))
                    }
                  }
                }
              } else {
                // KAR-126/3 ACCOUNT NOT ZERO, DEVID, SESSION FULL
                if (data.sessionId != fixedSessionId && data.sessionId != fixedSessionId2) {
                  complete(StatusCodes.Forbidden) // wrong session id
                } else {
                  if (data.deviceId != fixedDevIdMd5) {
                    complete(StatusCodes.BadRequest) // wrong device id
                  } else {
                    if (accountId != fixedAccountId) {
                      complete(StatusCodes.NoContent) // 204
                    } else {
                      complete(AccountSuggestedOffersResponse(fixedSessionId2))
                    }
                  }
                }
                //complete(StatusCodes.NotImplemented)
              }
          }
        }

    }

  }

  @Path("/options")
  @ApiOperation(position = 2, httpMethod = "OPTIONS", response = classOf[String], value = "KAR- options implemented")
  @ApiImplicitParams(Array())
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")))
  def myOptions = corsFilter(List("*")) {
    path("options") {
      options {
        complete(StatusCodes.Accepted -> "OK")
      }
    }
  }

}
