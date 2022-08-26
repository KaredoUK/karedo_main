package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.sample.Entities
import karedo.util.RouteDebug
import org.slf4j.LoggerFactory

trait Routes
  extends Entities

    with RouteDebug {

  override val logger = LoggerFactory.getLogger(classOf[Routes])

  override val routes: Route = Kar134.route ~ Kar135.route ~ Kar136.route ~
    Kar166.route ~ Kar188.route ~ Kar189.route ~ Kar194.route ~ Kar195.route


}

