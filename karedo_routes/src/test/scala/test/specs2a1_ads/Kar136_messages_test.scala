package test.specs2a1_ads

import common.AllTests
import karedo.entity.{UserAccount, UserApp, UserKaredos, UserMessages}
import karedo.util.Util
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
  * Created by pakkio on 10/21/16.
  */
@RunWith(classOf[JUnitRunner])
class Kar136_messages_test extends AllTests {


  private val my_tweet = Some("my_tweet")

  dbUserMessages.insertNew(UserMessages(Util.newUUID,presetAccount,tweet_text = my_tweet))

  "Kar136_messages" should {
    "* get messages" in {
      Get(s"/account/$presetAccount/messages?p=$presetAppId") ~> routesWithLogging ~> check {
        status.intValue() shouldEqual (HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
        var res=response
        val list = responseAs[List[UserMessages]]
        list should have size(1)
        list(0).tweet_text should be(my_tweet)
      }
    }
  }

}
