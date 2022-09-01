package karedo.actors

import karedo.entity.{UserAccount, UserApp, UserEmail, UserPrefs}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar143Actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar143Actor])

  def exec(email: String,
           email_code: String,
           accountId: Option[String]): Result[Error, APIResponse] = {

    logger.info(s"Email Verify\nemail: $email\nemail_code: $email_code\naccountId: $accountId")

    accountId match {
      case Some(account_id) => {
        dbUserAccount.find(account_id) match {
          case OK(userAccount) => {
            userAccount.email.filter(e => e.address == email) match {
              case eh::et => {
                eh.email_code match {
                  case Some(e_code) => {
                    if( e_code == email_code ) {
                      // EMail code matches
                      val restEmails = userAccount.email.filter(e => ! e.address.equals(email) )

                      val newEmail = eh.copy(valid = true, ts_validated = Some(now))
                      val newUserAccount = userAccount.copy(email = List(newEmail) ++ restEmails, temp = false )
                      dbUserAccount.update(newUserAccount) match {
                        case KO(error) => {
                          logger.error(error)
                          return OK(APIResponse(s"Verification failed. Email not allocated to correct account", 200))
                        }
                        case _ =>
                      }

                      dbUserEmail.find(email) match {
                        case OK(userEmail) => {
                          if( userEmail.account_id == account_id ) {
                            OK(APIResponse(s"Verification failed. EMail already verified", 200))
                          } else {
                            OK(APIResponse(s"Verification failed. EMail Registered to another account", 200))
                          }
                        }
                        case KO(_) => {
                          dbUserEmail.insertNew(UserEmail(email, account_id, true, Some(now), now)) match {
                            case KO(error) => {
                              logger.error(error)
                              OK(APIResponse(s"Verification failed. System error.", 200))
                            }
                            case OK(_) => OK(APIResponse("Verfication Successful. Welcome to Karedo", 200))
                          }
                        }
                      }
                    } else {
                      OK(APIResponse(s"Verification failed. Mismatch in Email and Account", 200))
                    }
                  }
                  case None => OK(APIResponse(s"Verification failed. Validation code not found", 200))
                }
              }
              case _ => OK(APIResponse(s"Verification failed. Unknown email address $email", 200))
            }
          }
          case KO(error) => {
            logger.error(error)
            OK(APIResponse(s"Verification failed. Account not found", 200))
          }
        }
      }
      case None => OK(APIResponse("Verification Failed. The URL is wrong", 200))
    }

  }
}