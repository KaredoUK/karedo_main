package karedo.persist.entity

import org.specs2.matcher.TryMatchers
import org.specs2.mutable.Specification
import utils.{DbCollections, MongoTestUtils}
import java.util.UUID

import org.joda.time.DateTime.now
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import karedo.common.result.{Result, OK, KO}

@RunWith(classOf[JUnitRunner])
class DbUserIntentSpec
  extends Specification
    with TryMatchers
    with DbCollections
    with MongoTestUtils {

  val intentDao = dbUserIntent

  sequential



  //intentDao.deleteAll()

  val uuid = UUID.randomUUID().toString
  val intent1 =
    IntentUnit(UUID.randomUUID(), "why_01", "what_01", "when_01", "where_01", now)

  val userIntent = UserIntent(uuid, List(intent1))

  val intent2 =
    IntentUnit(UUID.randomUUID(), "why_02", "what_02", "when_02", "where_02", now)


  "intentDao" should {

    "create an intent for a user" in {
      intentDao.insertNew(userIntent) must beOK
    }

    "update with new intent" in {
      intentDao.update(userIntent.copy(intents = List(intent1, intent2))) must beOK
    }

    "check if the intent stored is the right one" in {
      intentDao.find(uuid) match {
        case OK(x) => {
          x.id mustEqual(uuid)
          x.intents mustEqual(List(intent1, intent2))
        }
        case KO(err) => ko("find didn;t work")
      }
    }
  }


}
