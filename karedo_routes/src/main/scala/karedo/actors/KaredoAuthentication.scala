package karedo.actors

import karedo.entity.{UserAccount, UserApp}
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json.{JsObject, JsString}

/**
  * Created by pakkio on 10/9/16.
  */

trait KaredoAuthentication extends KaredoConstants {
  self: DbCollections  =>
  val logger = LoggerFactory.getLogger(classOf[KaredoAuthentication])

  def authenticate
  (
    accountId: String,
    deviceId: Option[String],
    applicationId: String,
    sessionId: Option[String],
    allowCreation: Boolean = true
  )
  (
    f: (Result[String, UserApp], Result[String, UserAccount], Int) => Result[Error, APIResponse]
  ) = {

    def anonymousCall(): Result[Error, APIResponse] = {
      val uapp = dbUserApp.find(applicationId)
      if (uapp.isOK) {
        val uAcct = dbUserAccount.find(uapp.get.account_id)
        // @TODO: Can;t be 200 OK here. @Chandan checking to see which is the right code.
        f(uapp, uAcct, HTTP_OK_200) // app already mapped to a valid account id
      }
      else {
        if (allowCreation) {
          // Create a new userAccount and connect it to applicationId
          val emptyAccount = UserAccount()
          val uacct = dbUserAccount.insertNew(emptyAccount)
          if (uacct.isOK) {

            val app = UserApp(id = applicationId, account_id = emptyAccount.id)
            val uNewApp = dbUserApp.insertNew(app)
            f(uNewApp, uacct, HTTP_OK_CREATED_201) // creating a new mapping

          }
          else KO(Error(s"Error ${uacct.err} while inserting new account"))
        }
        else OK(APIResponse("Application not found", HTTP_NOTFOUND_404))
      }


    }

    def accountProvided():
    Result[Error, APIResponse] = {
      val uacc = dbUserAccount.find(accountId)
      val uapp = dbUserApp.find(applicationId)
      if (uacc.isKO) {
        f(uapp,uacc,HTTP_NOTFOUND_404)
      } else {

        if (uapp.isOK) {
          val storedAccountId = uapp.get.account_id
          val uacc = dbUserAccount.find(storedAccountId)

          if (accountId != storedAccountId) {
            f(uapp, uacc, HTTP_OK_RESETCONTENT_205)
          } else {
            //val uacc = dbUserAccount.getById(accountId)
            if (sessionId.isDefined) {
              if (dbUserSession.find(sessionId.get).isOK) {
                f(uapp, uacc, HTTP_OK_200)
              } else {
                /* Mobile App: 206 indicates user login has expired. Mark for 4a. Login screen */
                f(uapp, uacc, HTTP_OK_PARTIALCONTENT_206)
              }
            } else {
              // not in a session
              f(uapp, uacc, HTTP_OK_PARTIALCONTENT_206)
            }
          }
        } else {
          val uacc = dbUserAccount.insertNew(UserAccount())
          val uapp = dbUserApp.insertNew(UserApp(applicationId, uacc.get.id))
          if (uapp.isKO) {
            logger.error(s"can't insert mapping")
          }
          f(uapp, uacc, HTTP_OK_CREATED_201)
        }
      }
    }
    // this is where authentication starts
    val ret = if (accountId == "0") {
      anonymousCall()
    }
    else {
      accountProvided()
    }

    ret

  }
  def jsonPair(name:String, value:String): String = {
    JsObject(name -> JsString(value)).toString
  }

  def JsonAccountIfNotTemp(uAcc: UserAccount): Option[String] = {
    if (!uAcc.temp) Some(uAcc.id)
    else None
  }

}
