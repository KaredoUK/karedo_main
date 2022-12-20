package karedo.routes.inform

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.inform.post_InformActor
import karedo.actors.sale.post_SaleActor
import karedo.routes.KaredoRoute
import karedo.routes.sale.post_SaleRoute.{doCall, exec}


/**
  * Created by charaj on 25/01/2017.
  */
object post_InformRoute
  extends KaredoRoute
    with post_InformActor
{
  def route = {
    path("inform") {
      post {
        entity( as[post_InformRequest]) {
          request =>
            doCall({
              exec(request)
            })
        }
      }
    }
  }

}

