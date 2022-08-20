package karedo.entity

import java.util.UUID

import karedo.entity.dao.{KO, OK}
import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserAppSpec
  extends Specification
      with EitherMatchers
      with MongoTestUtils {

    val test = new DbUserApp {}
    test.deleteAll()

    sequential

    "userapp should insert " in {
      val acctId = UUID.randomUUID()
      val appId = "app1"
      val r = UserApp(appId, acctId, map_confirmed = false)
      test.insertNew(r) must beOK
      val updated = r.copy(map_confirmed = true)
      test.update(updated)
      test.find(appId) match {
        case OK(x) => x.map_confirmed must beTrue
        case KO(error) => ko("update didn't work")
      }

    }
  }