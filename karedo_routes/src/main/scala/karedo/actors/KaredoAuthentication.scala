package karedo.actors

import karedo.entity.{UserAccount, UserApp}
import karedo.entity.dao.{KO, OK, Result}
import org.slf4j.LoggerFactory

/**
  * Created by pakkio on 10/9/16.
  */
trait KaredoAuthentication {
  self : KaredoCollections =>
  val logger = LoggerFactory.getLogger(classOf[KaredoAuthentication])

  def authenticate(accountId: String, deviceId: Option[String], applicationId: String, sessionId: Option[String],
                   f: (Result[String, UserApp], Result[String,UserAccount], Int) => Result[Error, APIResponse],
                   allowCreation: Boolean = true) = {

    def anonymousCall(): Result[Error, APIResponse] = {
      val uapp = dbUserApp.find(applicationId)
      if (uapp.isOK) {
        val uAcct = dbUserAccount.find(uapp.get.account_id)
        f(uapp, uAcct, 200) // app already mapped to a valid account id
      }
      else {
        if(allowCreation) {
          // Create a new userAccount and connect it to applicationId
          val emptyAccount = UserAccount()
          val uacct = dbUserAccount.insertNew(emptyAccount)
          if (uacct.isOK) {

            val app = UserApp(id = applicationId, account_id = emptyAccount.id)
            val uNewApp = dbUserApp.insertNew(app)
            f(uNewApp, uacct, 201) // creating a new mapping

          }
          else KO(Error(s"Error ${uacct.err} while inserting new account"))
        }
        else OK(APIResponse("Application not found",404))
      }


    }

    def accountProvided():
    Result[Error, APIResponse] = {
      val uapp = dbUserApp.find(applicationId)
      if (uapp.isOK) {
        val storedAccountId = uapp.get.account_id
        val uacc = dbUserAccount.find(storedAccountId)

        if (accountId != storedAccountId) {
          f(uapp, uacc,205)
        } else {
          //val uacc = dbUserAccount.getById(accountId)
          if (sessionId.isDefined) {
            if (dbUserSession.find(sessionId.get).isOK) {
              f(uapp, uacc, 200)
            } else {
              /* Mobile App: 206 indicates user login has expired. Mark for 4a. Login screen */
              f(uapp, uacc, 206)
            }
          } else {
            // not in a session
            f(uapp, uacc, 206)
          }
        }
      } else {
        val uacc = dbUserAccount.insertNew(UserAccount())
        val uapp = dbUserApp.insertNew(UserApp(applicationId, uacc.get.id))
        if (uapp.isKO) {
          logger.error(s"can't insert mapping")
        }
        f(uapp, uacc, 201)
      }
    }
    val ret = if (accountId == "0") {
      anonymousCall()
    }
    else {
      accountProvided()
    }

    ret

  }

}
