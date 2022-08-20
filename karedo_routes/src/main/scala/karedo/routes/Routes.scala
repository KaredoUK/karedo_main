package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.sample.Entities
import karedo.util.RouteDebug
import org.slf4j.LoggerFactory

trait Routes
  extends Entities
    with Kar134
    with Kar135
    with RouteDebug {

  override val logger = LoggerFactory.getLogger(classOf[Routes])

  override val routes: Route = kar134 ~ kar135


}

