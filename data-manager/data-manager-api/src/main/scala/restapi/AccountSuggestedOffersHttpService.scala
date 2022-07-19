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
abstract class AccountSuggestedOffersHttpService


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

  def route2 = intentWhat ~ prefNames


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
                  complete(AccountSuggestedOffersResponse(fixedSessionId, fixedListAds))
                }
                else {
                  // KAR-126/2 ACCOUNT0 - DEVID - SESSION FULL
                  if (data.sessionId != fixedSessionId && data.sessionId != fixedSessionId2) {
                    complete(StatusCodes.Forbidden) // wrong session id
                  } else {
                    if (data.deviceId != fixedDevIdMd5) {
                      complete(StatusCodes.BadRequest) // wrong device id
                    } else {
                      complete(AccountSuggestedOffersResponse(fixedSessionId2, fixedListAds))
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
                      complete(AccountSuggestedOffersResponse(fixedSessionId2, fixedListAds))
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

  @Path("/intent/what")
  @ApiOperation(position = 3, httpMethod = "GET", response = classOf[List[String]], value = "KAR-129 intent/what")
  @ApiImplicitParams(Array())
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")))
  def intentWhat =
    path("intent" / "what") {
      get {
        complete(List(
          "buy",
          "rent",
          "travel",
          "hire",
          "compare",
          "switch",
          "borrow",
          "visit"

        ))
      }
    }

  @Path("/pref/names")
  @ApiOperation(position = 4, httpMethod = "GET", response = classOf[List[(String,String)]], value = "KAR-127 pref/names")
  @ApiImplicitParams(Array())
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")))
  def prefNames =
    path("pref"/ "names") {
      get {
        complete(
          List(

            ("IAB22", "offers & discounts"),
            ("IAB18", "fashion & style"),
            ("IAB8", "food & drink"),
            ("IAB20", "travel & holidays"),
            ("IAB17", "sports"),
            ("IAB6", "family & children"),
            ("IAB7", "health & fitness"),
            ("IAB19", "computers & gadgets"),
            ("IAB4", "jobs & career"),
            ("IAB10", "home & garden"),
            ("IAB2", "cars & bikes"),
            ("IAB13", "personal finance"),
            ("IAB3", "business & finance"),
            ("IAB1", "arts & entertainment"),
            ("IAB14", "community & society"),
            ("IAB15", "science"),
            ("IAB16", "pets"),
            ("IAB5", "education"),
            ("IAB21", "property & housing"),
            ("IAB9", "hobbies & interests"),
            ("IAB11", "law, govt & politics"),
            ("IAB12", "news & current affairs"),
            ("IAB23", "religion & spirituality")
          )
        )
      }
    }

}
