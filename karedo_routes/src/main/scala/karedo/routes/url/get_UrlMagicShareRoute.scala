package karedo.routes.url

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.url.UrlMagicActor
import karedo.routes.KaredoRoute

object get_UrlMagicShareRoute extends KaredoRoute
  with UrlMagicActor {

  def route = {
    Route {
      path("shr" ) {
        optionalHeaderValueByName("User-Agent") {
          ua =>
            extractClientIP {
              ip =>
                get {
                  parameters('u, 'v) {
                    (url_code, hash_account) =>
                      doCall({
                        exec(url_code, hash_account, true, ua, ip)
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

