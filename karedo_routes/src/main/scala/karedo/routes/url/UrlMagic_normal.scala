package karedo.routes.url

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.url.UrlMagic
import karedo.routes.KaredoRoute

object UrlMagic_normal extends KaredoRoute
  with UrlMagic {

  def route = {
    Route {
      path("nrm" ) {
        get {
          parameters('u, 'v) {
            (url_code, hash_account) =>
              doCall({
                exec(url_code, hash_account, false)
              }
              )
          }
        }
      }

    }

  }
}

