package specs6_intent

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import common.AllTests
import karedo.entity._
import karedo.entity.dao.MongoConnection
import karedo.routes.Routes
import karedo.util.{KaredoJsonHelpers, _}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.{span, _}

/**
  * Created by crajah on 15/10/2016.
  */
class Specs6c_Intent extends AllTests

  with Kar169_getIntent_test
  with Kar171_putIntent_test
{

  val uuids:Array[String] = Array(getNewRandomID, getNewRandomID, getNewRandomID)

  val account_id = getNewRandomID
  val application_id = getNewRandomID
  val session_id = getNewRandomID

}
