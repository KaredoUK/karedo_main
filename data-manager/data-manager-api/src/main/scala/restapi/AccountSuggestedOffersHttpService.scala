package restapi


import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import spray.http.StatusCodes
import spray.routing._

//import spray.http.Uri.Path

// All APIs starting with /account go here
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

  def routeAccountSuggestedOffers =
    pathPrefix("account") {
      myOptions ~ postAccount

    }


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

  def myOptions = corsFilter(List("*")) {
    path("options") {
      options {
        complete(StatusCodes.Accepted -> "OK")
      }
    }
  }


}
