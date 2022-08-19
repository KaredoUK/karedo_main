package karedo.entity

import java.util.UUID

import org.specs2.matcher.EitherMatchers
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbKaredoChangeSpec
  extends Specification
    with EitherMatchers
    with MongoTestUtils {

  val test = new DbKaredoChange {}
  test.deleteAll()

  sequential

  "karedochange should insert a change" in {
    val acctId = UUID.randomUUID()
    val r = KaredoChange(UUID.randomUUID(),acctId,50,"transaction","info","GBP")
    val r2 = KaredoChange(UUID.randomUUID(),acctId,52,"transaction2","info2","GBP")


    test.insertNew(r) must beOK
    test.insertNew(r2) must beOK
    val changes: List[KaredoChange] = test.getChanges(acctId)
    changes.size must beEqualTo(2)
    changes(0).karedos must beEqualTo(50)
    changes(1).karedos must beEqualTo(52)
  }



}