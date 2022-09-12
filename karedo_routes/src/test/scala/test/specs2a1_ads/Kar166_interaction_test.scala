package test.specs2a1_ads

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.entity.UserAccount
import karedo.entity.UserAd
import karedo.util.Util
import karedo.entity.UserApp
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.scalatest.junit.JUnitRunner

/**
 * Created by pakkio on 10/21/16.
 */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar166_interaction_test extends AllTests {


//  val anAd = UserAd(application_id = presetAppId)
//  val request = Kar166Request("", List()).toJson.toString
//
//  "Kar166" should {
//    "* post user interactions with invalid account" in {
//
//      Post(s"/account/unknown/ad/interaction",
//        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~> check {
//          // returning 404 since error in authenticate
//          val st = status.intValue() // a different accountId
//          val res = response
//          st shouldEqual (HTTP_NOTFOUND_404)
//        }
//    }
//    "* post user interactions with valid account" in {
//
//      Post(s"/account/$presetAccount/ad/interaction",
//        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~> check {
//          // currently it is returning 205 since list is empty and so applId goes to blank already associated to
//          // a different accountId
//          val res = response
//          val st = status
//          status.intValue() shouldEqual (HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
//
//        }
//    }
//
//  }
}
