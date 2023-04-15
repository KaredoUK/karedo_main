package karedo.route.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.common.akka.DefaultActorSystem
import karedo.persist.entity.{Email, EmailVerify, Mobile, UserApp, UserEmail, UserMobile, UserProfile}
import karedo.common.jwt.JWTMechanic
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import karedo.common.result.{KO, OK, Result}

/**
  * Created by pakkio on 10/3/16.
  */
object post_SendCodeRoute extends KaredoRoute
  with post_SendCodeActor {

  def route = {
    Route {

      // POST /account
      path("account") {
              post {
                entity(as[post_SendCodeRequest]) {
                  request =>
                    doCall({
                      exec(request)
                    }
                    )
                }
              }
      }
    }
  }
}

trait post_SendCodeActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
    with DefaultActorSystem
    with JWTMechanic
{

  override val logger = LoggerFactory.getLogger(classOf[post_SendCodeActor])

  def exec(
            request: post_SendCodeRequest
          ): Result[Error, APIResponse] = {

    Try[Result[Error, APIResponse]] {
      if (request.application_id == null || request.application_id.equals("")) KO(Error(s"application_id is null"))
      if (request.first_name == null || request.first_name.equals("")) KO(Error(s"first_name is null"))
      if (request.last_name == null || request.last_name.equals("")) KO(Error(s"last_name is null"))
      if (request.msisdn == null || request.msisdn.equals("")) KO(Error(s"msisdn is null"))

      if (request.email == null || request.email.equals("")) KO(Error(s"email is null"))
      if (request.user_type == null || request.user_type.equals("")) KO(Error(s"user_type is null"))
      if (! request.user_type.equals("CUSTOMER")) KO(Error(s"user_type value is not CUSTOMER. Only one value is supported"))

      val applicationId = request.application_id.trim
      val firstName = request.first_name.trim
      val lastName = request.last_name.trim
      val msisdn = msisdnFixer(request.msisdn)
      val userType = request.user_type
      val email = request.email.toLowerCase.trim


      logger.debug(s"OK applicationId: $applicationId firstName: $firstName lastName: $lastName msisdn: $msisdn userType: $userType email: $email")

      dbUserApp.find(applicationId) match {
        case OK(userApp) => known_app(userApp, msisdn, email, (firstName, lastName))
        case KO(_) => unknown_app(applicationId,msisdn, email, (firstName, lastName))
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_THROWN_ERROR(f)
    }
  }

  def updateNameInProfile(account_id: String, name:(String, String)): Result[String, UserProfile] = {
    dbUserProfile.find(account_id) match {
      case OK(userProfile) => dbUserProfile.update(userProfile.copy(first_name = Some(name._1), last_name = Some(name._2), ts_updated = now)   )
      case KO(_) => dbUserProfile.insertNew(UserProfile(id = account_id, first_name = Some(name._1), last_name = Some(name._2)))
    }
  }

  def known_app(userApp: UserApp, msisdn:String, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserMobile.find(msisdn) match {
      case OK(userMobile) => known_app_known_mobile(userApp, userMobile, email, name)
      case KO(_) => known_app_unknown_mobile(userApp, msisdn, email, name)
    }
  }

  def known_app_known_mobile
  (userApp: UserApp, userMobile: UserMobile, email:String, name:(String, String)): Result[Error, APIResponse] = {
    if( userApp.account_id == userMobile.account_id ) known_app_known_mobile_matching_mobile(userApp, userMobile.account_id, email, name)
    else known_app_known_mobile_diff_mobile(userApp, userMobile, email, name)
  }

  def known_app_known_mobile_matching_mobile
  (userApp: UserApp, account_id: String, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserEmail.find(email) match {
      case OK(userEmail) => known_app_known_mobile_matching_mobile_known_email(userApp, account_id, userEmail, name)
      case KO(_) => known_app_known_mobile_matching_mobile_unknown_email(userApp, account_id, email, name)
    }
  }

  def known_app_known_mobile_matching_mobile_known_email
  (userApp: UserApp, account_id: String, userEmail:UserEmail, name:(String, String)): Result[Error, APIResponse] = {
    if( userEmail.account_id != account_id ) add_email_to_account_for_verification(userApp.id, account_id, userEmail.id)

    get_account_update_profile_check_temp(account_id, name)
  }

  def known_app_known_mobile_matching_mobile_unknown_email
  (userApp: UserApp, account_id: String, email: String, name:(String, String)): Result[Error, APIResponse] = {
    add_email_to_account_for_verification(userApp.id, account_id, email)

    get_account_update_profile_check_temp(account_id, name)
  }

  def known_app_known_mobile_diff_mobile
  (userApp: UserApp, userMobile:UserMobile, email:String, name:(String, String)): Result[Error, APIResponse] = {
    if( ! userApp.mobile_linked) {
      // @TODO: Copy code from known_app_unknown_mobile_known_email()

      val realUserAccount = dbUserAccount.find(userMobile.account_id).get

      if (realUserAccount.temp) MAKE_ERROR(s"Real UserAccount is set to temp=true for accountId: ${userMobile.account_id}")
      else {
        realUserAccount.mobile.filter(x => x.msisdn == userMobile.id) match {
          case Nil => MAKE_ERROR(s"Mobile ${userMobile.id} is not present in UserAccount accountId: ${userMobile.account_id}")
          case h :: t if h.valid != true => MAKE_ERROR(s"Mobile ${userMobile.id} in UserAccount accountId: ${userMobile.account_id} is not validated.")
          case _ => {
            moveKaredosBetweenAccounts(userApp.account_id, userMobile.account_id, None,
              s"From TEMP account ${userApp.account_id} to REAL account ${userMobile.account_id}")

            // Make the new link. Also set map_confirmed=true
            val newUserApp = userApp.copy(account_id = userMobile.account_id, mobile_linked = true, ts = now)
            dbUserApp.update(newUserApp)

            get_account_update_profile_check_temp(userMobile.account_id, name)
          }
        }
      }
    }
    else OK(
      APIResponse(
        ErrorRes(HTTP_CONFLICT_409, Some("msisdn"),
          "Account linked to ApplicationID and MSISDN are different. Eventhough ApplicationID is not temporary"
        ).toJson.toString, HTTP_CONFLICT_409))
  }

  def get_account_update_profile_check_temp(account_id: String, name:(String, String)): Result[Error, APIResponse] = {
    val userAccount = dbUserAccount.find(account_id).get
    updateNameInProfile(account_id, name)

    if( userAccount.temp) OK(APIResponse(SendCodeResponse(false, None).toJson.toString, HTTP_OK_200))
    else OK(APIResponse(SendCodeResponse(true, Some(account_id)).toJson.toString, HTTP_OK_200))
  }

  def known_app_unknown_mobile
  (userApp: UserApp, msisdn:String, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserEmail.find(email) match {
      case OK(userEmail) => known_app_unknown_mobile_known_email(userApp, msisdn, userEmail, name)
      case KO(_) => known_app_unknown_mobile_unknown_email(userApp, msisdn, email, name)
    }
  }

  def known_app_unknown_mobile_known_email
  (userApp: UserApp, msisdn: String, userEmail: UserEmail, name: (String, String)): Result[Error, APIResponse] = {
    if(userApp.account_id == userEmail.account_id) {
      add_mobile_to_account_for_verification(userEmail.account_id, msisdn)
      updateNameInProfile(userEmail.account_id, name)
      OK(APIResponse(SendCodeResponse(false, None).toJson.toString, HTTP_OK_200))
    }
    else {
      if( userApp.email_linked ) {
        add_mobile_to_account_for_verification(userApp.account_id, msisdn)
        add_email_to_account_for_verification(userApp.id, userApp.account_id, userEmail.id)

        updateNameInProfile(userApp.account_id, name)

        OK(APIResponse(SendCodeResponse(false, None).toJson.toString, HTTP_OK_200))
      } else {
        // @TODO: HOW DO WE KNOW THE UserAccount(UserApp) is Temp? SHouldn't we check.
        // @TODO: I Guess if email and mobile not linked to the account. It can be migrated.

        val realUserAccount = dbUserAccount.find(userEmail.account_id).get

        if (realUserAccount.temp) MAKE_ERROR(s"Real UserAccount is set to temp=true for accountId: ${userEmail.account_id}")
        else {
          realUserAccount.email.filter(x => x.address == userEmail.id) match {
            case Nil => MAKE_ERROR(s"Email ${userEmail.id} is not present in UserAccount accountId: ${userEmail.account_id}")
            case h :: t if h.valid != true => MAKE_ERROR(s"Email ${userEmail.id} in UserAccount accountId: ${userEmail.account_id} is not validated.")
            case _ => {
              moveKaredosBetweenAccounts(userApp.account_id, userEmail.account_id, None,
                s"From TEMP account ${userApp.account_id} to REAL account ${userEmail.account_id}")

              // Make the new link. Also set map_confirmed=true
              val newUserApp = userApp.copy(account_id = userEmail.account_id, email_linked = true, ts = now)

              dbUserApp.update(newUserApp)

              updateNameInProfile(realUserAccount.id, name)

              OK(APIResponse(SendCodeResponse(false, None).toJson.toString, HTTP_OK_200))
            }
          }
        }
      }
    }
  }

  def known_app_unknown_mobile_unknown_email
  (userApp: UserApp, msisdn: String, email: String, name: (String, String)): Result[Error, APIResponse] = {
    add_mobile_to_account_for_verification(userApp.account_id, msisdn)
    add_email_to_account_for_verification(userApp.id, userApp.account_id, email)

    updateNameInProfile(userApp.account_id, name)

    OK(APIResponse(SendCodeResponse(false, None).toJson.toString, HTTP_OK_200))
  }

  def unknown_app(application_id:String, msisdn:String, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserMobile.find(msisdn) match {
      case OK(userMobile) => unknown_app_known_mobile(application_id, userMobile, email, name)
      case KO(_) => unknown_app_unknown_mobile(application_id, msisdn, email, name)
    }
  }

  def unknown_app_known_mobile
  (application_id: String, userMobile: UserMobile, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserEmail.find(email) match {
      case OK(userEmail) => unknown_app_known_mobile_known_email(application_id, userMobile, userEmail, name)
      case KO(_) => unknown_app_known_mobile_unknown_email(application_id, userMobile, email, name)
    }
  }

  def unknown_app_known_mobile_known_email
  (application_id: String, userMobile: UserMobile, userEmail: UserEmail, name: (String, String)): Result[Error, APIResponse] = {

    if(userMobile.account_id == userEmail.account_id) {
      val account_id = userMobile.account_id

      // Add the account to UserApp
      dbUserApp.insertNew(UserApp(application_id, account_id, true, true, now))

      get_account_update_profile_check_temp(account_id, name)

    } else OK(
      APIResponse(
        ErrorRes(HTTP_CONFLICT_409, Some("msisdn"),
          "Account linked to ApplicationID and MSISDN are different. Eventhough ApplicationID is not temporary"
        ).toJson.toString, HTTP_CONFLICT_409))
  }

  def unknown_app_known_mobile_unknown_email
  (application_id: String, userMobile: UserMobile, email: String, name: (String, String)): Result[Error, APIResponse] = {
    val account_id = userMobile.account_id

    add_email_to_account_for_verification(application_id, account_id, email)

    dbUserApp.insertNew(UserApp(application_id, account_id, mobile_linked = true, email_linked = false, now))

    updateNameInProfile(account_id, name)

    OK(APIResponse(SendCodeResponse(true, Some(account_id)).toJson.toString, HTTP_OK_200))
  }

  def unknown_app_unknown_mobile
  (application_id: String, msisdn:String, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserEmail.find(email) match {
      case OK(userEmail) => unknown_app_unknown_mobile_known_email(application_id, msisdn, userEmail, name)
      case KO(_) => unknown_app_unknown_mobile_unknown_email(application_id, msisdn, email, name)
    }
  }

  def unknown_app_unknown_mobile_known_email
  (application_id: String, msisdn: String, userEmail: UserEmail, name:(String, String)): Result[Error, APIResponse] = {
    val account_id = userEmail.account_id

    add_mobile_to_account_for_verification(account_id, msisdn)

    dbUserApp.insertNew(UserApp(application_id, account_id, mobile_linked = false, email_linked = true, now))

    updateNameInProfile(account_id, name)

    OK(APIResponse(SendCodeResponse(false, None).toJson.toString, HTTP_OK_200))
  }

  def unknown_app_unknown_mobile_unknown_email
  (application_id: String, msisdn: String, email: String, name:(String, String)): Result[Error, APIResponse] = {

    val account_id = getNewRandomID

    createAndInsertNewAccount(account_id)

    add_mobile_to_account_for_verification(account_id, msisdn)
    add_email_to_account_for_verification(application_id, account_id, email)

    // Add the account to UserApp
    dbUserApp.insertNew(UserApp(application_id, account_id, mobile_linked = false, email_linked = false, now))

    updateNameInProfile(account_id, name)

    OK(APIResponse(SendCodeResponse(false, None).toJson.toString, HTTP_OK_200))
  }

  def add_email_to_account_for_verification(application_id: String, account_id: String, email: String, email_code: String = getNewRandomID): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val userAccount = dbUserAccount.find(account_id).get

      val emails = userAccount.email

      val verify_id = getNewRandomID
      dbEmailVerify.insertNew(EmailVerify(id = verify_id, account_id = account_id, application_id = application_id))

      val email_verify_url = s"${notification_base_url}/verify?e=${email}&c=${email_code}&v=${verify_id}"

      val email_subject = "Welcome to Karedo"
      val email_body = welcome.html.email_verify.render(email_verify_url).toString
      //s"Welcome to Karedo. \nYou're on your way to gaining from your attention. Click on [$email_verify_url] to verify your email"
      sendEmail(email, email_subject, email_body) onComplete {
        case Failure(e) => logger.error(s"Unable to send Email message to ${email}", e)
        case Success(s) =>
      }

      emails.filter(x => x.address == email) match {
        case Nil => {
          // Good email not already registered to account.
          val newUserAccount = userAccount.copy(email = emails ++ List(
            Email(address = email, email_code = Some(email_code), valid = false, ts_created = now, ts_validated = None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(SendCodeResponse(true, Some(account_id)).toJson.toString, HTTP_OK_200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
        case _ => {
          val restEmails = emails.filter(x => x.address != email)
          val newUserAccount = userAccount.copy(email = restEmails ++ List(
            Email(address = email, email_code = Some(email_code), valid = false, ts_created = now, ts_validated = None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(SendCodeResponse(true, Some(account_id)).toJson.toString, HTTP_OK_200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_THROWN_ERROR(f)
    }
  }

  def add_mobile_to_account_for_verification(account_id: String, msisdn: String, sms_code: String = getNewSMSCode): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val userAccount = dbUserAccount.find(account_id).get

      val msisdns = userAccount.mobile

      val sms_text = welcome.txt.sms_verify.render(sms_code).toString
      sendSMS(msisdn, sms_text) onComplete {
        case Failure(e) => logger.error(s"Unable to send SMS message to ${msisdn}", e)
        case Success(s) =>
      }

      msisdns.filter(x => x.msisdn == msisdn) match {
        case Nil => {
          // Good email not already registered to account.
          val newUserAccount = userAccount.copy(mobile = msisdns ++ List(
            Mobile(msisdn = msisdn, sms_code = Some(sms_code), valid = false, ts_created = now, ts_validated = None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(SendCodeResponse(true, Some(account_id)).toJson.toString, HTTP_OK_200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
        case _ => {
          val restMsisdns = msisdns.filter(x => x.msisdn != msisdn)
          val newUserAccount = userAccount.copy(mobile = restMsisdns ++ List(
            Mobile(msisdn = msisdn, sms_code = Some(sms_code), valid = false, ts_created = now, ts_validated =None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(SendCodeResponse(true, Some(account_id)).toJson.toString, 200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_THROWN_ERROR(f)
    }
  }


}