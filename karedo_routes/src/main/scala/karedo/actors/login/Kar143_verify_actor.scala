package karedo.actors.login

import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserEmail, UserMobile}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar143_verify_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar143_verify_actor])

  def exec(email: String,
           email_code: String,
           verifyId: Option[String]
          ): Result[Error, APIResponse] = {

    Try [Result[Error, APIResponse]] {

      logger.info(s"Email Verify\nmsisdn: ${email}\nsms_code: ${email_code}\nVerifyId: ${verifyId}")

      val emailVerify = dbEmailVerify.find(verifyId.get).get
      val account_id = emailVerify.account_id
      val application_id = emailVerify.application_id

      val userApp = dbUserApp.find(application_id).get
      val userAccount = dbUserAccount.find(account_id).get
      val accEmail = userAccount.email.filter(e => e.address == email).head
      val acc_email_code = accEmail.email_code.get

      if( acc_email_code == email_code ) {
        val restEmails = userAccount.email.filter(e => ! e.address.equals(email) )

        val newEmail = accEmail.copy(valid = true, ts_validated = Some(now))
        val newUserAccount = userAccount.copy(email = List(newEmail) ++ restEmails, temp = false )
        dbUserAccount.update(newUserAccount)


        dbUserEmail.find(email) match {
          case OK(userEmail) => {
            if( userEmail.account_id == account_id) OK(APIResponse("Verfication Successful. Welcome to Karedo", HTTP_OK_200, MIME_TEXT, headers = List(Cookie(HttpCookiePair(COOKIE_ACCOUNT, account_id)))))
            else OK(APIResponse(s"Verification failed. Email $email already registered another account", HTTP_OK_200, MIME_TEXT, headers = List(Cookie(HttpCookiePair(COOKIE_ACCOUNT, account_id)))))
          }
          case KO(_) => {
            dbUserEmail.insertNew(UserEmail(email, account_id, true, now, now))

            dbUserApp.update(userApp.copy(email_linked = true, ts = now))

            OK(APIResponse("Verfication Successful. Welcome to Karedo", HTTP_OK_200, MIME_TEXT, headers = List(Cookie(HttpCookiePair(COOKIE_ACCOUNT, account_id)))))
          }
        }
      } else {
        KO(Error("Validation Error. Code Doesn't match"))
      }
    } match {
      case Success(s) => s
      case Failure(f) => OK(APIResponse(s"Verification Failed. Something went wrong. ${f.toString}", HTTP_OK_200, MIME_TEXT))
    }
  }
}