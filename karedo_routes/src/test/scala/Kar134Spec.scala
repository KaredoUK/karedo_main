import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import karedo.entity.{DbUserAd, UserAd}
import karedo.entity.dao.MongoConnection
import karedo.routes.{Kar134, Routes}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.span
import scala.concurrent.duration._

/**
  * Created by pakkio on 05/10/16.
  */
class Kar134Spec extends WordSpec
  with MongoConnection
  with Routes
    with ScalatestRouteTest

    with Matchers {

    implicit val timeout = RouteTestTimeout(1000.second(span))

    val dbUserAds = new DbUserAd {}
    dbUserAds.deleteAll()
    dbUserAds.preload()


    "The webservice" should {



      val routes = kar134


      "* implements /account/0/applicationId" in {
        Get("/account/0/ads?p=app1") ~> routesWithLogging ~> check {
          responseAs[List[UserAd]] should have size(100)
        }

      }

  }

}
