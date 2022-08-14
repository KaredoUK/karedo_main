package karedo.sample

import akka.http.scaladsl.server.Route
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import sun.rmi.runtime.Log.LogFactory

trait Routes
  extends Entities
    with Kar134
    with RouteDebug {

  override val logger = LoggerFactory.getLogger(classOf[Routes])

  override val routes: Route = kar134

}

