package karedo.actors

import java.util.UUID

import karedo.entity._
import karedo.util.Util.now
import karedo.util._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import salat.annotations._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar141Actor_SendCode
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with DefaultActorSystem {

  override val logger = LoggerFactory.getLogger(classOf[Kar141Actor_SendCode])


  def exec(
           request: Kar141_SendCode_Req
          ): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val firstName = request.first_name
    val lastName = request.last_name
    val msisdn = request.msisdn
    val userType = request.user_type
    val email = request.email

    logger.info(s"OK applicationId: $applicationId firstName: $firstName lastName: $lastName msisdn: $msisdn userType: $userType email: $email")

    if(applicationId == null || applicationId.equals("")) KO(Error(s"application_id is null"))
    if(firstName == null || firstName.equals("")) KO(Error(s"first_name is null"))
    if(lastName == null || lastName.equals("")) KO(Error(s"last_name is null"))
    if(msisdn == null || msisdn.equals("")) KO(Error(s"msisdn is null"))

    if(email == null || email.equals("")) KO(Error(s"email is null"))
    if(userType == null || userType.equals("")) KO(Error(s"user_type is null"))
    if(! userType.equals("CUSTOMER")) KO(Error(s"user_type value is not CUSTOMER. Only one value is supported"))

    dbUserApp.find(applicationId) match {
      case OK(userApp) => {
        // UserApp(ApplicationId) found -> Application was used before. This is expected.
        dbUserMobile.find(msisdn) match {
          case OK(userMobile) => {
            // UserMobile(msisdn) found. -> Mobile number was used before. It's got an entry created.
            // Check if it links to UserApp(applicationID)

            if( userApp.account_id == userMobile.account_id) {
              // Linked UserApp and UserMobile through account_id
              // It means the Mobile Number has been registerd to the account before.
              // Must be a returngin user.

              dbUserEmail.find(email) match {
                case OK(userEmail) => {
                  // Found UserEmail(email).
                  // Chek is the Email matches the same account_id
                  if( userEmail.account_id == userMobile.account_id) {
                    // Linked UserEmail to UserMobile through account_id
                    // The Email has alo been registered before.
                    // Everything is fine. It;s just a returning user. Send them to Login Screen.
                    dbUserAccount.find(userMobile.account_id) match {
                      case OK(userAcct) => {
                        if( ! userAcct.temp ) {
                          OK(
                            APIResponse(
                              Kar141_SendCode_Res(true,
                                Some(userMobile.account_id)
                              ).toJson.toString, 200))
                        } else {
                          OK(
                            APIResponse(
                              Kar141_SendCode_Res(false,
                                None
                              ).toJson.toString, 200))
                        }
                      }
                      case KO(error) => MAKE_ERROR(error, s"Unable to get UserAccount for account ${userMobile.account_id}")
                    }
                  } else {
                    // Not Linked UserEmail and UserMobile. Email is used with this account the first time
                    // Add the email to the account.
                    addEmailToAccount(userMobile.account_id, email) match {
                      case OK(resp) => {
                        dbUserAccount.find(userMobile.account_id) match {
                          case OK(userAccount) => {
                            if( ! userAccount.temp ) {
                              OK(resp)
                            } else {
                              OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, 200))
                            }
                          }
                          case KO(error) => MAKE_ERROR(error, s"Unable to find account for ${userMobile.account_id}")
                        }
                      }
                      case KO(error) => KO(error)
                    }
                  }
                }
                case KO(_) => {
                  // UserEmail not found. New email address seen for teh first time.
                  // Add the email to UserEmail. Also, add email to UserAccount.
//                  dbUserEmail.insertNew(UserEmail(email, userMobile.account_id, false, Some(now), now)) match {
//                    case KO(error) => MAKE_ERROR(error, s"Unable to create an entry in UserEmail for email: $email")
//                    case OK(_) =>
//                  }
                  addEmailToAccount(userMobile.account_id, email)  match {
                    case OK(resp) => {
                      dbUserAccount.find(userMobile.account_id) match {
                        case OK(userAccount) => {
                          if( ! userAccount.temp ) {
                            OK(resp)
                          } else {
                            OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, 200))
                          }
                        }
                        case KO(error) => MAKE_ERROR(error, s"Unable to find account for ${userMobile.account_id}")
                      }
                    }
                    case KO(error) => KO(error)
                  }
                }
              }
            } else {
              // Not linked UserApp and UserMobile
              // -> Either ApplicationID is temporary or COULD BE AN ERROR.
              if( ! userApp.map_confirmed ) {
                // Temporary Account. Run with it.
                // Copy the karedos from the temporary account to teh real account.
                // Map the UserApp to the right account.
                // Send them to login screen.

                // @TODO: Check if isKO() and Bomb

                val tempUserAccount = dbUserAccount.find(userApp.account_id).get
                val realUserAccount = dbUserAccount.find(userMobile.account_id).get

                if( realUserAccount.temp ) MAKE_ERROR(s"Real UserAccount is set to temp=true for accountId: ${userMobile.account_id}")

                moveKaredosBetweenAccounts(userApp.account_id, userMobile.account_id, None,
                  s"From TEMP account ${userApp.account_id} to REAL account ${userMobile.account_id}" )

                // Make the new link. Also set map_confirmed=true
                val newUserApp = userApp.copy(account_id = userApp.account_id, map_confirmed = true, ts = now)

                dbUserApp.update(newUserApp) match {
                  case OK(_) => {
                    dbUserAccount.find(userMobile.account_id) match {
                      case OK(userAccount) => {
                        if( ! userAccount.temp ) {
                          OK(APIResponse(Kar141_SendCode_Res(true, Some(userMobile.account_id)).toJson.toString, 200))

                        } else {
                          OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, 200))
                        }
                      }
                      case KO(error) => MAKE_ERROR(error, s"Unable to find UserAccount for account ${userMobile.account_id}")
                    }
                  }
                  case KO(error) => KO(Error(s"Unable to map the UserApp to the right account. > $error"))
                }
              } else {
                // Not a Tempoary Account. BOMB here.
                OK(
                  APIResponse(
                    ErrorRes(409, Some("msisdn"),
                      "Account linked to ApplicationID and MSISDN are different. Eventhough ApplicationID is not temporary"
                    ).toJson.toString, 409))
              }
            }
          }
          case KO(_) => {
            // UserMobile not found. -> First time we are seeing this mobile number.
//            dbUserMobile.insertNew(UserMobile(msisdn, userApp.account_id, false, Some(now), now))
//            match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserMobile for accountID: ${userApp.account_id}")
//            case _ => }

            dbUserEmail.find(email) match {
              case OK(userEmail) => {
                // UserEmail found -> user might have registerd with email beofre but using a new mobile.
                // Check is the UserEmail and UserApp linked.
                if( userApp.account_id == userEmail.account_id) {
                  // Linked UserEMail and UserApp through account_id.
                  // OK so we have seen this user before but not the mobile number.
                  // Perhaps, the user wants to change the mobile number. ???

                  dbUserAccount.find(userEmail.account_id) match {
                    case OK(userAccount) => {
                      // UserEmail found. So email is mapped before. But not mobile.
                      // Add the mobile to the UserAccount.
                      // Copy the Karedos.
                      addMobileToAccount(userEmail.account_id, msisdn) match {
                        case OK(_) => OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, 200))
                        case KO(error) => KO(error)
                      }
                    }
                    case KO(_) => KO(Error(s"UserEmail has an entry for email: $email but UserAccount cannot be found for ${userEmail.account_id}"))
                  }
                } else {
                    // Different account mapped to applicationID and email
                  if( ! userApp.map_confirmed ) {
                    // Temporary UserApp(applicationID)
                    // Move details form UserEmail.acount_id to UserApp.account_id
                    // Leave map_confirmed as that is only for mobile.
                    val tempUserAccount = dbUserAccount.find(userApp.account_id).get
                    val realUserAccount = dbUserAccount.find(userEmail.account_id).get

                    if( realUserAccount.temp ) MAKE_ERROR(s"Real UserAccount is set to temp=true for accountId: ${userEmail.account_id}")

                    moveKaredosBetweenAccounts(userApp.account_id, userEmail.account_id, None,
                      s"From TEMP account ${userApp.account_id} to REAL account ${userEmail.account_id}" )

                    // Make the new link. Also set map_confirmed=true
                    val newUserApp = userApp.copy(account_id = userApp.account_id, ts = now)

                    dbUserApp.update(newUserApp) match {
                      case OK(_) => {
                        OK(
                          APIResponse(
                            Kar141_SendCode_Res(false,
                              None
                            ).toJson.toString, 200))
                      }
                      case KO(error) => KO(Error(s"Unable to map the UserApp to the right account. > $error"))
                    }
                  } else {
                    // Not temporary account
                    // Validate everything.
                    addMobileToAccount(userApp.account_id, msisdn) match {
                      case OK(_) =>
                      case KO(error) => return KO(error)
                    }
                    addEmailToAccount(userApp.account_id, email) match {
                      case OK(_) =>
                      case KO(error) => return KO(error)
                    }

                    OK(
                      APIResponse(
                        Kar141_SendCode_Res(false,
                          None
                        ).toJson.toString, 200))
                  }
                }
              }
              case KO(_) => {
                // UserEmail not found.
                // Create new UserEmail entry
//                dbUserEmail.insertNew(UserEmail(email, userApp.account_id, false, Some(now), now))
//                match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserEmail for accountID: ${userApp.account_id}")
//                case _ => }

                addMobileToAccount(userApp.account_id, msisdn) match {
                  case OK(_) =>
                  case KO(error) => return KO(error)
                }
                addEmailToAccount(userApp.account_id, email) match {
                  case OK(_) =>
                  case KO(error) => return KO(error)
                }

                OK(
                  APIResponse(
                    Kar141_SendCode_Res(false,
                      None
                    ).toJson.toString, 200))
              }
            }
          }
        }
      }
      case KO(_) => {
        // ApplicationId not found
        // Should never get here. GET /ads should have set up some UserApp(applicationID)
        dbUserMobile.find(msisdn) match {
          case OK(userMobile) => {
            // UserMobile found. We can do something with this.
            dbUserEmail.find(email) match {
              case OK(userEmail) => {
                // Both UserEMail and UserMobile found.
                // Make sure they are the same
                if( userMobile.account_id == userEmail.account_id) {
                  val account_id = userMobile.account_id

                  // Add the account to UserApp
                  dbUserApp.insertNew(UserApp(applicationId, account_id, true, now))
                  match { case KO(error) => return MAKE_ERROR(error, s"Unbale to update UserApp for applictionIS: ${applicationId}")
                  case _ => }

                  dbUserAccount.find(account_id) match {
                    case OK(userAccount) => {
                      if( ! userAccount.temp ) {
                        OK(APIResponse(Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, 200))
                      } else {
                        OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, 200 ))
                      }
                    }
                    case KO(error) => MAKE_ERROR(error, s"Unable to find UserAccount for $account_id")
                  }
                } else {
                  // Conflict
                  OK(
                    APIResponse(
                      ErrorRes(409, Some("msisdn"),
                        "Account linked to ApplicationID and MSISDN are different. Eventhough ApplicationID is not temporary"
                      ).toJson.toString, 409))
                }
              }
              case KO(_) => {
                // No UserMail Found. User UserMobile
                val account_id = userMobile.account_id

                // Create new UserEmail entry
//                dbUserEmail.insertNew(UserEmail(email, account_id, false, Some(now), now))
//                match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserEmail for accountID: ${account_id}")
//                case _ => }

//                addMobileToAccount(account_id, msisdn)
                addEmailToAccount(account_id, email) match {
                  case OK(_) =>
                  case KO(error) => return KO(error)
                }

                // Add the account to UserApp
                dbUserApp.insertNew(UserApp(applicationId, account_id, true, now))
                match { case KO(error) => return MAKE_ERROR(error, s"Unbale to update UserApp for applictionIS: ${applicationId}")
                case _ => }

                OK(
                  APIResponse(
                    Kar141_SendCode_Res(true,
                      Some(account_id)
                    ).toJson.toString, 200))

              }
            }
          }
          case KO(_) => {
            // UserMobile not found. Maybe luck with UserEMail
            dbUserEmail.find(email) match {
              case OK(userEmail) => {
                // Found UserEmail. User it.
                val account_id = userEmail.account_id

                // UserMobile not found. -> First time we are seeing this mobile number.
//                dbUserMobile.insertNew(UserMobile(msisdn, account_id, false, Some(now), now))
//                match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserMobile for accountID: ${account_id}")
//                case _ => }

                addMobileToAccount(account_id, msisdn) match {
                  case OK(_) =>
                  case KO(error) => return KO(error)
                }
//                addEmailToAccount(account_id, email)

                // Add the account to UserApp
                dbUserApp.insertNew(UserApp(applicationId, account_id, true, now))
                match { case KO(error) => return MAKE_ERROR(error, s"Unbale to update UserApp for applictionIS: ${applicationId}")
                case _ => }

                OK(
                  APIResponse(
                    Kar141_SendCode_Res(false,
                      None
                    ).toJson.toString, 200))
              }
              case KO(_) => {
                // Create a new UserAccount
                val account_id = getNewRandomID
                val new_user_account = UserAccount( account_id
                  , None
                  , None
                  , DEFAULT_CUSTOMER_TYPE
                  , List()
                  , List()
                  , true
                  , now
                  , now
                )
                dbUserAccount.insertNew( new_user_account)
                match { case KO(error) => return MAKE_ERROR(error, s"Unable to create a new UserAccount")
                case _ => }

                // UserMobile not found. -> First time we are seeing this mobile number.
//                dbUserMobile.insertNew(UserMobile(msisdn, account_id, false, Some(now), now))
//                match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserMobile for accountID: ${account_id}")
//                case _ => }

                // Create new UserEmail entry
//                dbUserEmail.insertNew(UserEmail(email, account_id, false, Some(now), now))
//                match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserEmail for accountID: ${account_id}")
//                case _ => }

                addMobileToAccount(account_id, msisdn) match {
                  case OK(_) =>
                  case KO(error) => return KO(error)
                }
                addEmailToAccount(account_id, email) match {
                  case OK(_) =>
                  case KO(error) => return KO(error)
                }

                // Add the account to UserApp
                dbUserApp.insertNew(UserApp(applicationId, account_id, true, now))
                match { case KO(error) => return MAKE_ERROR(error, s"Unbale to update UserApp for applictionIS: ${applicationId}")
                case _ => }

                OK(
                  APIResponse(
                    Kar141_SendCode_Res(false,
                      None
                    ).toJson.toString, 200))
              }
            }
          }
        }
      }
    }
  }

  def addEmailToAccount(account_id: String, email: String, email_code:String = getNewRandomID): Result[Error, APIResponse] = {
    dbUserAccount.find(account_id) match {
      case OK(userAccount) => {

        // Add a new UserEmail entry
//        dbUserEmail.insertNew(UserEmail(email, account_id, false, Some(now), now))
//        match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserEmail for accountID: ${account_id}") }

        val emails = userAccount.email

        val email_verify_url = s"${notification_base_url}/verify?e=${email}&c=${email_code}&a=${account_id}"

        val email_subject = "Welcome to Karedo"
        val email_body = s"Welcome to Karedo. \nYou're on your way to gaining from your attention. Click on [$email_verify_url] to verify your email"
        sendEmail(email, email_subject, email_body) onComplete {
          case Failure(error) => KO(error)
          case Success(s) => println(s.get)
        }

        emails.filter(x => x.address == email) match {
          case Nil => {
            // Good email not already registered to account.
            val newUserAccount = userAccount.copy(email = emails ++ List(Email(email, Some(email_code), false, now, None)))

            dbUserAccount.update(newUserAccount) match {
              case OK(_) => OK( APIResponse( Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, 200))
              case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
            }
          }
          case _ => {
            val restEmails = emails.filter(x => x.address != email)
            val newUserAccount = userAccount.copy(email = restEmails ++ List(Email(email, Some(email_code), false, now, None)))

            dbUserAccount.update(newUserAccount) match {
              case OK(_) => OK( APIResponse( Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, 200))
              case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
            }
          }
        }
      }
      case KO(error) => MAKE_ERROR(error, s"ERROR: addEmailToAccount: Non existing account: ${account_id}")
    }
  }

  def addMobileToAccount(account_id: String, msisdn: String, sms_code:String = getNewSMSCode): Result[Error, APIResponse] = {
    dbUserAccount.find(account_id) match {
      case OK(userAccount) => {

        // Add a new UserMobile entry
//        dbUserMobile.insertNew(UserMobile(msisdn, account_id, false, Some(now), now))
//        match { case KO(error) => MAKE_ERROR(error, s"Unbale to update UserMobile for accountID: ${account_id}") }

        val msisdns = userAccount.mobile

        val sms_text = s"Welcome to Karedo. You're on your way to gaining from your attention. Code is [$sms_code]. Start the Karedo App to activate it"
        sendSMS(msisdn, sms_text) onComplete {
          case Failure(error) => KO(error)
          case Success(s) => println(s.get)
        }

        msisdns.filter(x => x.msisdn == msisdn) match {
          case Nil => {
            // Good email not already registered to account.
            val newUserAccount = userAccount.copy(mobile = msisdns ++ List(Mobile(msisdn, Some(sms_code), false, now, None, true)))

            dbUserAccount.update(newUserAccount) match {
              case OK(_) => OK( APIResponse( Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, 200))
              case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
            }
          }
          case _ => {
            val restMsisdns = msisdns.filter(x => x.msisdn != msisdn)
            val newUserAccount = userAccount.copy(mobile = restMsisdns ++ List(Mobile(msisdn, Some(sms_code), false, now, None, true)))

            dbUserAccount.update(newUserAccount) match {
              case OK(_) => OK( APIResponse( Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, 200))
              case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
            }
          }
        }
      }
      case KO(error) => MAKE_ERROR(error, s"ERROR: addMobileToAccount: Non existing account: ${account_id}")
    }
  }

  def moveKaredosBetweenAccounts(from: String, to: String, karedos: Option[Double], text: String = "", currency: String = "KAR"): Result[Error, String] = {
    val fromUserKaredo = dbUserKaredos.find(from).get
    val toUserKaredo = dbUserKaredos.find(to).get

    val act_karedo = karedos match {
      case Some(k) => k
      case None => {
        // All Karedos.
        fromUserKaredo.karedos
      }
    }

    if( fromUserKaredo.karedos < act_karedo) MAKE_ERROR(s"From UserKaredos doesn't have enough Karedos accountId: ${from}")

    val new_fromUserKaredo = fromUserKaredo.copy(karedos = fromUserKaredo.karedos - act_karedo, ts = now)
    val new_toUserKaredo = toUserKaredo.copy(karedos = toUserKaredo.karedos + act_karedo, ts = now)

    dbUserKaredos.update(new_fromUserKaredo)
    match { case KO(error) => return MAKE_ERROR(error, s"Unable to update Karedos. accountId: $from")
    case _ => }

    dbUserKaredos.update(new_toUserKaredo)
    match { case KO(error) => return MAKE_ERROR(error, s"Unable to update Karedos. accountId: $to")
    case _ => }

    dbKaredoChange.insertNew(KaredoChange(from, to, -act_karedo,
      TRANS_TYPE_TRANSFERED, s"Moved Karedos: $karedos from $from to $to -> $text", currency, now ))
    match { case KO(error) => return MAKE_ERROR(error, "Unable to update KaredoChange")
    case _ => }

    dbKaredoChange.insertNew(KaredoChange(to, from, act_karedo,
      TRANS_TYPE_TRANSFERED, s"Moved Karedos: $karedos from $from to $to -> $text", currency, now ))
    match { case KO(error) => return MAKE_ERROR(error, "Unable to update KaredoChange")
    case _ => }

    OK("Complete")

  }

  def sendSMS(msisdn: String, text: String): Future[Result[Error, String]] =  {
    import spray.client.pipelining._
    import spray.http._

    val pipeline: HttpRequest => Future[HttpResponse] = {
      addHeader("Authorization", s"AccessKey $notification_sms_auth_accesskey") ~> sendReceive //~> unmarshal[String]
    }

    pipeline {
      Post(
        Uri(notification_sms_server_endpoint), SMSRequest(msisdn, notification_sms_sender, text).toJson.toString)
    } map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        MAKE_ERROR(s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}")
      } else {
        OK(s"[SMS] Sent a sms, response from service is $httpResponse")
      }
    }
  }

  def sendEmail(email: String, subject: String, body: String): Future[Result[Error, String]] =  {
    import spray.client.pipelining._
    import spray.http.{BasicHttpCredentials, FormData, HttpResponse}

    val requestPipeline = addCredentials(BasicHttpCredentials("api", s"$notification_email_auth_accesskey")) ~> sendReceive

    requestPipeline {
      Post(
        notification_email_server_endpoint,
        FormData(
          Map(
            "from" -> notification_email_sender,
            "to" -> email,
            "subject" -> subject,
            "html" -> body
          )
        )
      )
    } map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        MAKE_ERROR(s"[EMAIL] Got an error response is ${httpResponse.entity.asString}")
      } else {
        OK(s"[EMAIL] Email sent correctly answer is ${httpResponse.entity}")
      }
    }
  }

}