package specs2a1_ads

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import karedo.entity.UserAccount

/**
  * Created by pakkio on 10/21/16.
  */
trait Kar166_interaction_test {
  self : Specs2a1_AdLaunch =>

  "Kar166" should {
    "* post user interactions with invalid account" in {
      val request = Kar166Request(List()).toJson.toString
      Post(s"/account/unknown/ad/interaction",
        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~> check {
        // currently it is returning 205 since list is empty and so applId goes to blank already associated to
        // a different accountId
        status.intValue() shouldEqual (500)
      }
    }
    "* post user interactions with valid account" in {
      val account = getNewRandomID
      dbUserAccount.insertNew(UserAccount(id = account))

      val request = Kar166Request(List()).toJson.toString
      Post(s"/account/$account/ad/interaction",
        HttpEntity(ContentTypes.`application/json`, request)) ~> routesWithLogging ~> check {
        // currently it is returning 205 since list is empty and so applId goes to blank already associated to
        // a different accountId
        status.intValue() shouldEqual (HTTP_OK_RESETCONTENT_205)
      }
    }

  }
}
