package karedo.route.actors

import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.common.{DbCollections, KaredoConstants, KaredoUtils}
import org.slf4j.LoggerFactory
import spray.json.{JsObject, JsString}
import karedo.common.result.{KO, OK, Result}

/**
  * Created by pakkio on 10/9/16.
  */

trait KaredoAuthentication extends KaredoConstants with KaredoUtils {
  self: DbCollections  =>
  override val logger = LoggerFactory.getLogger(classOf[KaredoAuthentication])

  def authenticate
  (
    accountId: String,
    deviceId: Option[String],
    applicationId: String,
    sessionId: Option[String],
    allowCreation: Boolean,
    respondAnyway: Boolean = false
  )
  (
    f: (Result[String, UserApp], Result[String, UserAccount], Int) => Result[Error, APIResponse]
  ) = {

    def authenticationSequence:Result[Error, APIResponse] = {
      dbUserApp.find(applicationId) match {
        case OK(userApp) => {
          if (! accountId.equals("0") ) { // Found Application, account_id NOT 0
            dbUserAccount.find(accountId) match {
              case OK(userAccount) => {
                if (userApp.account_id == accountId ) { // Found Account, Matches Application
                  sessionId match {
                    case Some(sessId) => { // Found session_id
                      dbUserSession.find(sessId) match { // Found Session, Matches Account & Application
                        case OK(userSession) => {
                          if (userSession.account_id == accountId) {
                            f(OK(userApp), OK(userAccount), HTTP_OK_200)
                          } else {
                            if( respondAnyway ) {
                              f(OK(userApp), OK(userAccount), HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
                            } else {
                              KO(Error(err = "Session Expired. Request Login", code = HTTP_UNAUTHORISED_401))
                            }
                          }
                        }
                        case KO(_) => { // Not in Session.
                          if( respondAnyway ) {
                            f(OK(userApp), OK(userAccount), HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
                          } else {
                            KO(Error(err = "Session Expired. Request Login", code = HTTP_UNAUTHORISED_401))
                          }
                        }
                      }
                    }
                    case None => { // No session_id
                      if( respondAnyway ) {
                        f(OK(userApp), OK(userAccount), HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
                      } else {
                        KO(Error(err = "Session Expired. Request Login", code = HTTP_UNAUTHORISED_401))
                      }
                    }
                  }
                } else { // Found Account, NOT Matches Application
                  if( respondAnyway ) {
                    f(OK(userApp), OK(userAccount), HTTP_OK_RESETCONTENT_205)
                  } else {
                    KO(Error(err = "Account Conflict. Start with Send Code", code = HTTP_CONFLICT_409))
                  }
                }
              }
              case KO(_) => { // Not found Account.
                if(respondAnyway) {
                  dbUserAccount.find(userApp.account_id) match {
                    case OK(userAccount) => {
                      f(OK(userApp), OK(userAccount), HTTP_OK_200)
                    }
                    case KO(_) => {
                      if( allowCreation ) {
                        val account_id = userApp.account_id
                        val userAccount = createAndInsertNewAccount(account_id)

                        f(OK(userApp), userAccount, HTTP_OK_CREATED_201)
                      } else {
                        KO(Error(err = "Account Not Found. Start with Send Code", code = HTTP_NOTFOUND_404))
                      }
                    }
                  }
                } else {
                  if( allowCreation ) {
                    val account_id = userApp.account_id
                    val userAccount = createAndInsertNewAccount(account_id)

                    f(OK(userApp), userAccount, HTTP_OK_CREATED_201)
                  } else {
                    KO(Error(err = "Account Not Found. Start with Send Code", code = HTTP_NOTFOUND_404))
                  }
                }
              }
            }
          } else {   // Found Application & account_id == 0
            if( respondAnyway ) {
              dbUserAccount.find(userApp.account_id) match {
                case OK(userAccount) => {
                  f(OK(userApp), OK(userAccount), HTTP_OK_200)
                }
                case KO(_) => {
                  if( allowCreation ) {
                    val account_id = userApp.account_id
                    val userAccount = createAndInsertNewAccount(account_id)

                    f(OK(userApp), userAccount, HTTP_OK_CREATED_201)
                  } else {
                    KO(Error(err = "Account Not Found. Internal Error.", code = HTTP_SERVER_ERROR_500))
                  }
                }
              }
            } else {
              KO(Error(err = "Account ID is 0. Bad Request.", code = HTTP_BADREQUEST_400))
            }
          }
        }
        case KO(_) => { // Not Found Application
          if( allowCreation ) {
            val account_id = getNewRandomID
            createAndInsertNewAccount(account_id) match {
              case OK(userAccount) => {
                val app = UserApp(id = applicationId, account_id = account_id)

                dbUserApp.insertNew(app) match {
                  case OK(userApp) => {
                    f(OK(userApp), OK(userAccount), HTTP_OK_CREATED_201)
                  }
                  case KO(_) => KO(Error(err = "Error Creating New Application. Reset App.", code = HTTP_SERVER_ERROR_500))
                }
              }
              case KO(_) => KO(Error(err = "Error Creating New Account. Reset App.", code = HTTP_SERVER_ERROR_500))
            }
          } else {
            KO(Error(err = "Account ID is 0. Bad Request.", code = HTTP_BADREQUEST_400))
          }
        }
      }
    }

    /* Original COde. Commented out by Chandan Rajah
    def anonymousCall(): Result[Error, APIResponse] = {
      val uapp = dbUserApp.find(applicationId)

      if (uapp.isOK) {
        val uAcct = dbUserAccount.find(uapp.get.account_id)

        if( uAcct.isOK) f(uapp, uAcct, HTTP_OK_200) // app already mapped to a valid account id
        else KO(Error(err = "Error Finding New Account. Reset App.", code = HTTP_SERVER_ERROR_500))
      }
      else {
        if (allowCreation) {
          // Create a new userAccount and connect it to applicationId
          val account_id = getNewRandomID
          val uacct = createAndInsertNewAccount(account_id)

          if (uacct.isOK) {
            val app = UserApp(id = applicationId, account_id = account_id)
            val uNewApp = dbUserApp.insertNew(app)

            f(uNewApp, uacct, HTTP_OK_CREATED_201) // creating a new mapping
          }
          else KO(Error(err = "Error Creating New Account. Reset App.", code = HTTP_SERVER_ERROR_500))
        }
        else {
          //OK(APIResponse("Application Not Registered", HTTP_NOTFOUND_404))
          KO(Error(err = "Application Not Found. Restart Send Code.", code = HTTP_NOTFOUND_404))
        }
      }
    }

    def accountProvided(): Result[Error, APIResponse] = {
      val uacc = dbUserAccount.find(accountId)
      val uapp = dbUserApp.find(applicationId)

      uapp match {
        case OK(userApp) => {}
        case KO(_) => {
          // Unknown Application
        }
      }

      if (uacc.isKO) {
        // f(uapp,uacc,HTTP_NOTFOUND_404)
        KO(Error(err = "Account Not Found. Restart Send Code.", code = HTTP_NOTFOUND_404))
      } else {

        if (uapp.isOK) {
          val storedAccountId = uapp.get.account_id
          val uacc = dbUserAccount.find(storedAccountId)

          if (accountId != storedAccountId) {
            // f(uapp, uacc, HTTP_OK_RESETCONTENT_205)
            KO(Error(err = "User Account Not Matching. Restart Send Code.", code = HTTP_OK_RESETCONTENT_205))
          } else {
            //val uacc = dbUserAccount.getById(accountId)
            if (sessionId.isDefined) {
              if (dbUserSession.find(sessionId.get).isOK) {
                f(uapp, uacc, HTTP_OK_200)
              } else {
                /* Mobile App: 206 indicates user login has expired. Mark for 4a. Login screen */
                // f(uapp, uacc, HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
                KO(Error(err = "Session Expired. Request Login", code = HTTP_OK_PARTIALCONTENT_NOTINASESSION_206))
              }
            } else {
              // not in a session
              // f(uapp, uacc, HTTP_OK_PARTIALCONTENT_NOTINASESSION_206)
              KO(Error(err = "Session Expired. Request Login", code = HTTP_OK_PARTIALCONTENT_NOTINASESSION_206))
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
    */

    authenticationSequence
  }

  def jsonPair(name:String, value:String): String = {
    JsObject(name -> JsString(value)).toString
  }

  def JsonAccountIfNotTemp(uAcc: UserAccount): Option[String] = {
    if (!uAcc.temp) Some(uAcc.id)
    else None
  }

}
