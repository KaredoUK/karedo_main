package common

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.persist.entity.{UserAccount, UserApp, UserKaredos}
import karedo.persist.entity.dao._
import karedo.route.routes.Routes
import karedo.route.util._
import org.scalatest.{Matchers, WordSpec}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by pakkio on 10/21/16.
  */
trait AllTests extends WordSpec
  with MongoConnection_Casbah
  with Routes
  with ScalatestRouteTest
  with DbCollections
  with KaredoJsonHelpers
  with KaredoConstants
  with KaredoIds
  with Matchers {

  override val logger = LoggerFactory.getLogger(classOf[AllTests])

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(1000.second(span))

  // can't clear everything otherwise tests cannot go in parallel (!)
  DbDAOParams.tablePrefix = "TestRoutes_"

}
