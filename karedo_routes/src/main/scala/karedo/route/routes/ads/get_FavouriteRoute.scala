package karedo.route.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object get_FavouriteRoute extends KaredoRoute
  with get_FavouriteActor {

  def route = {
    Route {
      path("account" / Segment / "ad" / "favourite") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              get {
                parameters('p, 's ?) {
                  (applicationId, sessionId) =>
                    doCall({
                      exec(accountId, deviceId, applicationId, sessionId)
                    }
                    )
                }
              }
          }
      }
    }
  }
}

trait get_FavouriteActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[get_FavouriteActor])
  //accountId, deviceId, applicationId, sessionId
  def exec(accountId: String, deviceId: Option[String], applicationId:String, sessionId: Option[String]): Result[Error, APIResponse] = {
    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          dbUserFavourite.find(accountId) match {
            case OK(userFav) => {
              OK(APIResponse(FavouriteListResponse(userFav.entries).toJson.toString, code))
            }
            case KO(_) => {
              OK(APIResponse("", code))
            }
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}
