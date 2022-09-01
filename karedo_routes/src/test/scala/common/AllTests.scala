package common

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.entity.dao.MongoConnection
import karedo.routes.Routes
import karedo.util.{DbCollections, KaredoConstants, KaredoJsonHelpers}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._


/**
  * Created by pakkio on 10/21/16.
  */
trait AllTests extends WordSpec
  with MongoConnection
  with Routes
  with ScalatestRouteTest
  with DbCollections
  with KaredoJsonHelpers
  with KaredoConstants

  with Matchers {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(1000.second(span))

  // can't clear everything otherwise tests cannot go in parallel (!)
  // mongoClient.dropDatabase(mongoDbName)


}
