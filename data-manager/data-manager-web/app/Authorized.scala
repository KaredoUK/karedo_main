import api.{DataManagerRestClient, DataManagerApiClient}
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import controllers.routes
import java.util.UUID
import org.slf4j.LoggerFactory
import parallelai.wallet.config.ConfigConversions._
import play.api.Logger
import play.api.mvc.{Results, SimpleResult, Filter, RequestHeader}
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.Try
import ExecutionContext.Implicits.global

object AuthorizedFilter {
  def apply(implicit bindingModule: BindingModule) = new AuthorizedFilter
}

class AuthorizedFilter(implicit val bindingModule: BindingModule) extends Filter with Injectable with Results {
  val logger = LoggerFactory.getLogger(classOf[AuthorizedFilter])

  val accessiblePagesRegexpList = injectProperty[List[String]]("auth.accessible.pages") map { regex => new Regex(s"${regex}$$") }
  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient

  override def apply(next: RequestHeader => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {

    implicit val header = request

    def readUUIDCookie(headerName: String) : Option[UUID] = Try {
      request.cookies.get(headerName) map { cookie => UUID.fromString(cookie.value) }
    } recover {
      case _ => None
    } get

    def notAuthorized : Future[SimpleResult] = {
      logger.debug(s"Not authorized {}", request.path)
      successful(Redirect(routes.RegistrationController.register.absoluteURL(false)))
    }

    def pass : Future[SimpleResult] = {
      next(request)
    }

    if(authorizationRequired(request)) {
      val userId = readUUIDCookie("uuid")
      val applicationId = readUUIDCookie("applicationId")

      logger.debug( s"Userid ${userId} applicationID ${applicationId}")

      (userId, applicationId) match {
        case (Some(userId), Some(applicationId)) =>
            dataManagerApiClient.findUserForApplication(applicationId) flatMap {
              _ match {
                case Some(user) =>
                  logger.error( s"User is ${user}")
                  if (user.info.userId.equals(userId))
                    pass
                  else
                    notAuthorized

                case None =>
                  logger.error( s"User with id is ${userId} is not in the DB")
                  notAuthorized
              }
            }

        case (_, _) => notAuthorized
      }
    }
    else {
      pass
    }
  }

  private def authorizationRequired(request: RequestHeader) = {
    val actionInvoked: String = request.path
    accessiblePagesRegexpList.collectFirst {
      case regex if(regex.findFirstIn(actionInvoked).isDefined) =>
        logger.debug(s"In white list $actionInvoked for regex ${regex.pattern.pattern}")
        Some(regex)
    }.isEmpty
  }
}