package karedo.routes

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.server.Route
import karedo.sample.Entities
import karedo.util.RouteDebug
import org.slf4j.LoggerFactory

trait Routes
  extends Entities
    with Kar134
    with RouteDebug {

  override val logger = LoggerFactory.getLogger(classOf[Routes])

  override val routes: Route = kar134


}

