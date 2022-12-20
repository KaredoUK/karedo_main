package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.ads.post_InteractionActor
import karedo.actors.ads.post_ShareDataActor
import karedo.actors.ads.post_FavouriteActor
import karedo.actors.ads.get_FavouriteActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object post_InteractionRoute extends KaredoRoute
  with post_InteractionActor {

  def route = {
    Route {

      // POST /account/{account_id}/ad/interaction
      path("account" / Segment / "ad" / "interaction") {
        accountId =>
              optionalHeaderValueByName("X_Identification") {
                deviceId =>
                  post {
                    entity(as[post_InteractionRequest]) {
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

object post_ShareDataRoute extends KaredoRoute
  with post_ShareDataActor {

  def route = {
    Route {
      path("account" / Segment / "ad" / "share_data") {
        accountId =>
              optionalHeaderValueByName("X_Identification") {
                deviceId =>
                  post {
                    entity(as[post_ShareDataRequest]) {
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

