package karedo.actors.login

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.util.Util.now
import karedo.util.{Result, _}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar141_SendCode_actor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
    with DefaultActorSystem
{

  override val logger = LoggerFactory.getLogger(classOf[Kar141_SendCode_actor])

  def updateNameInProfile(account_id: String, name:(String, String)): Result[String, UserProfile] = {
    dbUserProfile.find(account_id) match {
      case OK(userProfile) => dbUserProfile.update(userProfile.copy(first_name = name._1, last_name = name._2, ts_updated = now)   )
      case KO(_) => dbUserProfile.insertNew(UserProfile(id = account_id, first_name = name._1, last_name = name._2))
    }
  }

  def known_app(userApp: UserApp, msisdn:String, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserMobile.find(msisdn) match {
      case OK(userMobile) => known_app_known_mobile(userApp, userMobile, email, name)
      case KO(_) => known_app_unknown_mobile(userApp, msisdn, email, name)
    }
  }

  def known_app_known_mobile(userApp: UserApp, userMobile: UserMobile, email:String, name:(String, String)): Result[Error, APIResponse] = {
    if( userApp.account_id == userMobile.account_id ) known_app_known_mobile_matching_mobile(userMobile.account_id, email, name)
    else known_app_known_mobile_diff_mobile(userApp, userMobile, email, name)
  }

  def known_app_known_mobile_matching_mobile(account_id: String, email:String, name:(String, String)): Result[Error, APIResponse] = {
    dbUserEmail.find(email) match {
      case OK(userEmail) => known_app_known_mobile_matching_mobile_known_email(account_id, userEmail, name)
      case KO(_) => known_app_known_mobile_matching_mobile_unknown_email(account_id, email, name)
    }
  }

  def known_app_known_mobile_matching_mobile_known_email(account_id: String, userEmail:UserEmail, name:(String, String)): Result[Error, APIResponse] = {
    if( userEmail.account_id != account_id ) add_email_to_account_for_verification(account_id, userEmail.id)

    get_account_update_profile_check_temp(account_id, name)
  }

  def known_app_known_mobile_matching_mobile_unknown_email(account_id: String, email: String, name:(String, String)): Result[Error, APIResponse] = {
    add_email_to_account_for_verification(account_id, email)

    get_account_update_profile_check_temp(account_id, name)
  }

  def known_app_known_mobile_diff_mobile
  (userApp: UserApp, userMobile:UserMobile, email:String, name:(String, String)): Result[Error, APIResponse] = {
    if( ! userApp.mobile_linked) {
      val tempUserAccount = dbUserAccount.find(userApp.account_id).get
      val realUserAccount = dbUserAccount.find(userMobile.account_id).get

      if (realUserAccount.temp) MAKE_ERROR(s"Real UserAccount is set to temp=true for accountId: ${userMobile.account_id}")

      moveKaredosBetweenAccounts(userApp.account_id, userMobile.account_id, None,
        s"From TEMP account ${userApp.account_id} to REAL account ${userMobile.account_id}")

      // Make the new link. Also set map_confirmed=true
      val newUserApp = userApp.copy(account_id = userApp.account_id, mobile_linked = true, ts = now)
      dbUserApp.update(newUserApp)

      get_account_update_profile_check_temp(userMobile.account_id, name)
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

    if( userAccount.temp) OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, HTTP_OK_200))
    else OK(APIResponse(Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, HTTP_OK_200))
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
      OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, HTTP_OK_200))
    }
    else {
      if( userApp.email_linked ) {
        add_mobile_to_account_for_verification(userApp.account_id, msisdn)
        add_email_to_account_for_verification(userApp.account_id, userEmail.id)

        updateNameInProfile(userApp.account_id, name)

        OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, HTTP_OK_200))
      } else {
        val tempUserAccount = dbUserAccount.find(userApp.account_id).get
        val realUserAccount = dbUserAccount.find(userEmail.account_id).get

        if (realUserAccount.temp) MAKE_ERROR(s"Real UserAccount is set to temp=true for accountId: ${userEmail.account_id}")

        moveKaredosBetweenAccounts(userApp.account_id, userEmail.account_id, None,
          s"From TEMP account ${userApp.account_id} to REAL account ${userEmail.account_id}")

        // Make the new link. Also set map_confirmed=true
        val newUserApp = userApp.copy(account_id = userApp.account_id, email_linked= true,  ts = now)

        dbUserApp.update(newUserApp)

        updateNameInProfile(realUserAccount.id, name)

        OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, HTTP_OK_200))
      }
    }
  }

  def known_app_unknown_mobile_unknown_email
  (userApp: UserApp, msisdn: String, email: String, name: (String, String)): Result[Error, APIResponse] = {
    add_mobile_to_account_for_verification(userApp.account_id, msisdn)
    add_email_to_account_for_verification(userApp.account_id, email)

    updateNameInProfile(userApp.account_id, name)

    OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, HTTP_OK_200))
  }

  def unknown_app(msisdn:String, email:String, name:(String, String))(application_id: String): Result[Error, APIResponse] = {
      dbUserMobile.find(msisdn) match {
        case OK(userMobile) => unknown_app_known_mobile(userMobile, email, name)(application_id)
        case KO(_) => unknown_app_unknown_mobile(msisdn, email, name)(application_id)
      }
  }

  def unknown_app_known_mobile
  (userMobile: UserMobile, email:String, name:(String, String))(application_id: String): Result[Error, APIResponse] = {
    dbUserEmail.find(email) match {
      case OK(userEmail) => unknown_app_known_mobile_known_email(userMobile, userEmail, name)(application_id)
      case KO(_) => unknown_app_known_mobile_unknown_email(userMobile, email, name)(application_id)
    }
  }

  def unknown_app_known_mobile_known_email
  (userMobile: UserMobile, userEmail: UserEmail, name: (String, String))(application_id: String): Result[Error, APIResponse] = {

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
  (userMobile: UserMobile, email: String, name: (String, String))(application_id: String): Result[Error, APIResponse] = {
    val account_id = userMobile.account_id

    add_email_to_account_for_verification(account_id, email)

    dbUserApp.insertNew(UserApp(application_id, account_id, mobile_linked = true, email_linked = false, now))

    updateNameInProfile(account_id, name)

    OK(APIResponse(Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, HTTP_OK_200))
  }

  def unknown_app_unknown_mobile(msisdn:String, email:String, name:(String, String))(application_id: String): Result[Error, APIResponse] = {
    dbUserEmail.find(email) match {
      case OK(userEmail) => unknown_app_unknown_mobile_known_email(msisdn, userEmail, name)(application_id)
      case KO(_) => unknown_app_unknown_mobile_unknown_email(msisdn, email, name)(application_id)
    }
  }

  def unknown_app_unknown_mobile_known_email
  (msisdn: String, userEmail: UserEmail, name:(String, String))(application_id: String): Result[Error, APIResponse] = {
    val account_id = userEmail.account_id

    add_mobile_to_account_for_verification(account_id, msisdn)

    dbUserApp.insertNew(UserApp(application_id, account_id, mobile_linked = false, email_linked = true, now))

    updateNameInProfile(account_id, name)

    OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, HTTP_OK_200))
  }

  def unknown_app_unknown_mobile_unknown_email
  (msisdn: String, email: String, name:(String, String))(application_id: String): Result[Error, APIResponse] = {

    val account_id = getNewRandomID

    createAndInsertNewAccount(account_id)

    add_mobile_to_account_for_verification(account_id, msisdn)
    add_email_to_account_for_verification(account_id, email)

    // Add the account to UserApp
    dbUserApp.insertNew(UserApp(application_id, account_id, mobile_linked = false, email_linked = false, now))

    updateNameInProfile(account_id, name)

    OK(APIResponse(Kar141_SendCode_Res(false, None).toJson.toString, HTTP_OK_200))
  }

  def exec(
            request: Kar141_SendCode_Req
          ): Result[Error, APIResponse] = {

    Try[Result[Error, APIResponse]] {
      val applicationId = request.application_id
      val firstName = request.first_name
      val lastName = request.last_name
      val msisdn = request.msisdn
      val userType = request.user_type
      val email = request.email

      if (applicationId == null || applicationId.equals("")) KO(Error(s"application_id is null"))
      if (firstName == null || firstName.equals("")) KO(Error(s"first_name is null"))
      if (lastName == null || lastName.equals("")) KO(Error(s"last_name is null"))
      if (msisdn == null || msisdn.equals("")) KO(Error(s"msisdn is null"))

      if (email == null || email.equals("")) KO(Error(s"email is null"))
      if (userType == null || userType.equals("")) KO(Error(s"user_type is null"))
      if (!userType.equals("CUSTOMER")) KO(Error(s"user_type value is not CUSTOMER. Only one value is supported"))

      logger.info(s"OK applicationId: $applicationId firstName: $firstName lastName: $lastName msisdn: $msisdn userType: $userType email: $email")

      dbUserApp.find(applicationId) match {
        case OK(userApp) => known_app(userApp, msisdn, email, (firstName, lastName))
        case KO(_) => unknown_app(msisdn, email, (firstName, lastName))(applicationId)
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_ERROR(f)
    }
  }

  def add_email_to_account_for_verification(account_id: String, email: String, email_code: String = getNewRandomID): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val userAccount = dbUserAccount.find(account_id).get

      val emails = userAccount.email

      val email_verify_url = s"${notification_base_url}/verify?e=${email}&c=${email_code}&a=${account_id}"

      val email_subject = "Welcome to Karedo"
      val email_body = s"Welcome to Karedo. \nYou're on your way to gaining from your attention. Click on [$email_verify_url] to verify your email"
      sendEmail(email, email_subject, email_body) onComplete {
        case Failure(error) => throw error
        case Success(s) =>
      }

      emails.filter(x => x.address == email) match {
        case Nil => {
          // Good email not already registered to account.
          val newUserAccount = userAccount.copy(email = emails ++ List(
            Email(address = email, email_code = Some(email_code), valid = false, ts_created = now, ts_validated = None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, HTTP_OK_200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
        case _ => {
          val restEmails = emails.filter(x => x.address != email)
          val newUserAccount = userAccount.copy(email = restEmails ++ List(
            Email(address = email, email_code = Some(email_code), valid = false, ts_created = now, ts_validated = None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, HTTP_OK_200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_ERROR(f)
    }
  }

  def add_mobile_to_account_for_verification(account_id: String, msisdn: String, sms_code: String = getNewSMSCode): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val userAccount = dbUserAccount.find(account_id).get

      val msisdns = userAccount.mobile

      val sms_text = s"Welcome to Karedo. You're on your way to gaining from your attention. Code is [$sms_code]. Start the Karedo App to activate it"
      sendSMS(msisdn, sms_text) onComplete {
        case Failure(error) => throw error
        case Success(s) =>
      }

      msisdns.filter(x => x.msisdn == msisdn) match {
        case Nil => {
          // Good email not already registered to account.
          val newUserAccount = userAccount.copy(mobile = msisdns ++ List(
            Mobile(msisdn = msisdn, sms_code = Some(sms_code), valid = false, ts_created = now, ts_validated = None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, HTTP_OK_200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
        case _ => {
          val restMsisdns = msisdns.filter(x => x.msisdn != msisdn)
          val newUserAccount = userAccount.copy(mobile = restMsisdns ++ List(
            Mobile(msisdn = msisdn, sms_code = Some(sms_code), valid = false, ts_created = now, ts_validated =None)))

          dbUserAccount.update(newUserAccount) match {
            case OK(_) => OK(APIResponse(Kar141_SendCode_Res(true, Some(account_id)).toJson.toString, 200))
            case KO(error) => MAKE_ERROR(error, s"Unbale to update UserAccount for accountID: ${account_id}")
          }
        }
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_ERROR(f)
    }
  }


}