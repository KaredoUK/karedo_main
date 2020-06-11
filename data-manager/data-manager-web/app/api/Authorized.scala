package api

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import controllers.routes
import org.slf4j.LoggerFactory
import parallelai.wallet.config.ConfigConversions._
import play.api.mvc.{Filter, RequestHeader, Results, SimpleResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.util.Try
import scala.util.matching.Regex

object authorization {
  val logger = LoggerFactory.getLogger(this.getClass)

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

    logger.debug( s"Userid ${userId} applicationID ${applicationId}")

    (userId, applicationId) match {
      case (Some(userId), Some(applicationId)) =>
        dataManagerApiClient.findUserForApplication(applicationId) map {
          _ match {
            case Some(user) =>
              logger.error(s"User is ${user}")
              if (user.info.userId.equals(userId))
                true
              else
                false

            case None =>
              logger.error(s"User with id is ${userId} is not in the DB")
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
  val logger = LoggerFactory.getLogger(classOf[AuthorizedFilter])

  val accessiblePagesRegexpList = injectProperty[List[String]]("auth.accessible.pages") map { regex => new Regex(s"${regex}$$") }
  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient

  override def apply(next: RequestHeader => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {

    implicit val header = request

    if(authorizationRequired(request)) {
      isKnownUser(request, dataManagerApiClient) flatMap { isKnown =>
         if(isKnown) {
           if(hasValidatedPwd(request)) {
             next(request)
           } else {
             logger.debug("Recognised user but no validated password in cache {}", request.path)
             successful(Redirect(routes.RegistrationController.passwordRequest(request.uri).absoluteURL(false), Map("redirectTo" -> Seq(request.uri))))
           }
         } else {
           logger.debug("Not a recognised user {}", request.path)
           successful(Redirect(routes.RegistrationController.register.absoluteURL(false)).withSession())
        }
      }
    } else {
      next(request)
    }
  }

  private def hasValidatedPwd(request: RequestHeader): Boolean = request.session.get("pwd").isDefined

  private def authorizationRequired(request: RequestHeader): Boolean = {
    val actionInvoked: String = request.path
    accessiblePagesRegexpList.collectFirst {
      case regex if(regex.findFirstIn(actionInvoked).isDefined) =>
        logger.debug(s"In white list $actionInvoked for regex ${regex.pattern.pattern}")
        Some(regex)
    }.isEmpty
  }
}