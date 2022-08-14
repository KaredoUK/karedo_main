package karedo.sample

import akka.http.scaladsl.server.Route

object Routes
  extends Entities
    with RouteSample
    with RouteDebug {

  override val routes: Route = Kar134.route

}

