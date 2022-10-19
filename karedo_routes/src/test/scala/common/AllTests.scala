package common

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.entity.{UserAccount, UserApp, UserKaredos}
import karedo.entity.dao.{DbMongoDAO, MongoConnection}
import karedo.routes.Routes
import karedo.util._
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


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
  with KaredoIds
  with Matchers {

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(1000.second(span))

  // can't clear everything otherwise tests cannot go in parallel (!)
  DbMongoDAO.tablePrefix = "TestRoutes_"

}
