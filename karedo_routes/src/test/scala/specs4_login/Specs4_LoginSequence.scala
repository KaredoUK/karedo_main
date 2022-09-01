package specs4_login

import common.AllTests
import karedo.entity._

/**
  * Created by pakkio on 05/10/16.
  */
class Specs4_LoginSequence extends AllTests

  with Kar138_login_test
  with Kar141_SendCode_Test {

  val acctId = getNewRandomID
  val appId = getNewRandomID

  dbUserApp.insertNew(UserApp(appId,acctId))
  dbUserAccount.insertNew(UserAccount(acctId,password=Some("pippo")))

  var sessionId: String = ""

}
