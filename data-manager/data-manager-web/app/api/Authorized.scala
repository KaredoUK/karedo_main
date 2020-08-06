package api

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import controllers.routes
import org.slf4j.LoggerFactory
import parallelai.wallet.config.ConfigConversions._
import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Results, SimpleResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.util.Try
import scala.util.matching.Regex

object authorization {

  val COOKIE_UUID = "uuid"
  val COOKIE_APP_ID = "applicationId"

  def readUUIDCookie(cookieName: String)(implicit requestHeaders: RequestHeader) : Option[UUID] =
    Try {
      requestHeaders.cookies.get(cookieName) map { cookie => UUID.fromString(cookie.value) }
    } recover {
      case _ => None
    } get

  def isKnownUser(requestHeaders: RequestHeader, dataManagerApiClient: DataManagerApiClient): Future[Boolean] = {
    implicit val _ = requestHeaders

    val userId = readUUIDCookie(COOKIE_UUID)
    val applicationId = readUUIDCookie(COOKIE_APP_ID)

    Logger.debug( s"IsKnownUser: Userid ${userId} applicationID ${applicationId}")

    (userId, applicationId) match {
      case (Some(userId), Some(applicationId)) =>
        dataManagerApiClient.findUserForApplication(applicationId) map {
          _ match {
            case Some(user) =>
              Logger.debug(s"User is ${user}")
              if (user.info.userId.equals(userId)) {
                Logger.debug("UserId matches")
                true
              } else {
                Logger.debug("UserId DOESN'T match")
                false
              }

            case None =>
              Logger.debug(s"User with id is ${userId} is not in the DB")
              false
          }
        }

      case (_, _) => successful(false)
    }
  }
}

object AuthorizedFilter {
  def apply(implicit bindingModule: BindingModule) = new AuthorizedFilter
}

import api.authorization._

class AuthorizedFilter(implicit val bindingModule: BindingModule) extends Filter with Injectable with Results {

  val accessiblePagesRegexpList = injectProperty[List[String]]("auth.accessible.pages") map { regex => new Regex(s"${regex}$$") }
  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient

  override def apply(next: RequestHeader => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {

    implicit val header = request

    if(authorizationRequired(request)) {
      isKnownUser(request, dataManagerApiClient) flatMap { isKnown =>
         if(isKnown) {
           if(hasValidatedPwd(request)) {
             Logger.debug( s"Recognised user with validated password in cache ${request.path}")
             next(request)
           } else {
             Logger.debug( s"Recognised user but no validated password in cache ${request.path}")
             successful(Redirect(routes.RegistrationController.passwordRequest(request.uri).absoluteURL(false)))
           }
         } else {
           Logger.debug( s"Not a recognised user ${request.path}")
           successful(Redirect(routes.RegistrationController.register.absoluteURL(false)).withSession())
        }
      }
    } else {
      next(request)
    }
  }

  private def hasValidatedPwd(request: RequestHeader): Boolean = request.session.get("password").isDefined

  private def authorizationRequired(request: RequestHeader): Boolean = {
    val actionInvoked: String = request.path
    accessiblePagesRegexpList.collectFirst {
      case regex if(regex.findFirstIn(actionInvoked).isDefined) =>
        Logger.debug(s"In white list $actionInvoked for regex ${regex.pattern.pattern}")
        Some(regex)
    }.isEmpty
  }
}