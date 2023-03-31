package karedo.route.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp, UserFavourite}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object post_FavouriteRoute extends KaredoRoute
  with post_FavouriteActor {

  def route = {
    Route {
      path("account" / Segment / "ad" / "favourite") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[post_FavouriteRequest]) {
                  request =>
                    doCall({
                      exec(accountId, deviceId, request)
                    }
                    )
                }
              }
          }

      }
    }
  }
}

trait post_FavouriteActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[post_FavouriteActor])

  def exec(accountId: String, deviceId: Option[String], request: post_FavouriteRequest): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = request.session_id
    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, Some(sessionId), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          dbUserFavourite.find(accountId) match {
            case OK(userFav) => {
              val newFavs = request.favourites.map(f => f.copy(ts = None, favourite = None))
              val oldFavs = userFav.entries.filter(f => ! newFavs.contains(f.copy(ts = None, favourite = None)))
              val allfavs = request.favourites ++ oldFavs

              dbUserFavourite.update(userFav.copy(entries = allfavs))
            }
            case KO(_) => {
              dbUserFavourite.insertNew(UserFavourite(accountId, request.favourites))
            }
          }

          OK(APIResponse("", code))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}
