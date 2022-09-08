package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.ads.Kar166_interaction_actor
import karedo.actors.ads.Kar167_share_data_actor
import karedo.actors.ads.Kar165_postFavourite_actor
import karedo.actors.ads.Kar165_getFavourite_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar166_interaction extends KaredoRoute
  with Kar166_interaction_actor {

  def route = {
    Route {

      // POST /account/{account_id}/ad/interaction
      path("account" / Segment / "ad" / "interaction") {
        accountId =>
          parameters('s ?) {
            sessionId => {
              optionalHeaderValueByName("X_Identification") {
                deviceId =>
                  post {
                    entity(as[Kar166Request]) {
                      request =>
                        doCall({
                          exec(accountId, sessionId, deviceId, request)
                        }
                        )
                    }
                  }
              }
            }
          }

      }
    }
  }
}

object Kar167_share_data extends KaredoRoute
  with Kar167_share_data_actor {

  def route = {
    Route {
      path("account" / Segment / "ad" / "share_data") {
        accountId =>
          parameters('s ?) {
            sessionId => {
              optionalHeaderValueByName("X_Identification") {
                deviceId =>
                  post {
                    entity(as[Kar167Request]) {
                      request =>
                        doCall({
                          exec(accountId, sessionId, deviceId, request)
                        }
                        )
                    }
                  }
              }
            }
          }

      }
    }
  }
}

object Kar165_postFavourite extends KaredoRoute
  with Kar165_postFavourite_actor {

  def route = {
    Route {
      path("account" / Segment / "ad" / "favourite") {
        accountId =>
          parameters('s ?) {
            sessionId => {
              optionalHeaderValueByName("X_Identification") {
                deviceId =>
                  post {
                    entity(as[Kar165Request]) {
                      request =>
                        doCall({
                          exec(accountId, sessionId, deviceId, request)
                        }
                        )
                    }
                  }
              }
            }
          }

      }
    }
  }
}

object Kar165_getFavourite extends KaredoRoute
  with Kar165_getFavourite_actor {

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

