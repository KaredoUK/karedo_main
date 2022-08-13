package karedo.entity

import java.util.UUID

import org.specs2.matcher.TryMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserAppSpec
  extends Specification
      with TryMatchers
      with MongoTestUtils {

    val test = new DbUserApp {}

    sequential

    "userapp should insert " in {
      val acctId = UUID.randomUUID()
      val appId = UUID.randomUUID()
      val r = UserApp(appId, acctId, false)
      test.insertNew(appId,r) must beSuccessfulTry
      val updated = r.copy(map_confirmed = true)
      test.update(appId,updated)
      val reread = test.getById(appId)
      reread must beSome
      reread.get.map_confirmed  must beTrue

    }
  }