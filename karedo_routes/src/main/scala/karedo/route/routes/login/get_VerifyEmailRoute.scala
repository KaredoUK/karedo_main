package karedo.route.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.UserEmail
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers}
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}

/**
  * Created by pakkio on 10/3/16.
  */
object get_VerifyEmailRoute extends KaredoRoute
  with get_VerifyEmailActor {

  def route = {
    Route {

      // GET /verify?e={email}&c={email_code}&a={account_id}
      path("verify" ) {
        get {
          parameters( 'e, 'c, 'v ? ) {
            ( email, email_code, verifyId ) =>
              doCall(
                {
                  exec(email, email_code, verifyId)
                }
              )

          }
        }
      }
    }
  }
}

trait get_VerifyEmailActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[get_VerifyEmailActor])

  def exec(email: String,
           email_code: String,
           verifyId: Option[String]
          ): Result[Error, APIResponse] = {

    Try [Result[Error, APIResponse]] {

      logger.debug(s"Email Verify\nmsisdn: ${email}\nsms_code: ${email_code}\nVerifyId: ${verifyId}")

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
            if( userEmail.account_id == account_id) OK(APIResponse(welcome.html.email_verify_success.render().toString,
              HTTP_OK_200, MIME_HTML, headers = List(Cookie(HttpCookiePair(COOKIE_ACCOUNT, account_id)))))
            else OK(APIResponse(welcome.html.email_verify_failure.render(email).toString, HTTP_OK_200, MIME_HTML, headers = List(Cookie(HttpCookiePair(COOKIE_ACCOUNT, account_id)))))
          }
          case KO(_) => {
            dbUserEmail.insertNew(UserEmail(email, account_id, true, now, now))

            dbUserApp.update(userApp.copy(email_linked = true, ts = now))

            OK(APIResponse(welcome.html.email_verify_success.render().toString, HTTP_OK_200, MIME_HTML, headers = List(Cookie(HttpCookiePair(COOKIE_ACCOUNT, account_id)))))
          }
        }
      } else {
        OK(APIResponse(welcome.html.email_verify_failure.render(email).toString, HTTP_OK_200, MIME_HTML, headers = List(Cookie(HttpCookiePair(COOKIE_ACCOUNT, account_id)))))
      }
    } match {
      case Success(s) => s
      case Failure(f) => OK(APIResponse(welcome.html.email_verify_error.render(f.toString + "</br>" + f.getStackTrace.map(_.toString).reduce(_ + "</br>" + _)).toString, HTTP_OK_200, MIME_HTML))
    }
  }
}