package karedo.routes.inform

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.inform.Kar12_postInform_actor
import karedo.actors.sale.Kar186_postSale_actor
import karedo.routes.KaredoRoute
import karedo.routes.sale.Kar186_postSale.{doCall, exec}


/**
  * Created by charaj on 25/01/2017.
  */
object Kar12_postInform
  extends KaredoRoute
    with Kar12_postInform_actor
{
  def route = {
    path("inform") {
      post {
        entity( as[Kar12Req]) {
          request =>
            doCall({
              exec(request)
            })
        }
      }
    }
  }

}

