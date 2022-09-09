package karedo.actors.termsabout

import akka.http.scaladsl.model.headers
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.UrlAccess
import karedo.util._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait TermsAbout extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[TermsAbout])

  def exec(termsAbout: String): Result[Error, APIResponse] = {
    termsAbout match {
      case GET_TERM => OK(APIResponse(msg = "<html><body><h1>TERMS</h1></body></html>", code = HTTP_OK_200, mime = MIME_HTML))
      case GET_ABOUT  => OK(APIResponse(msg = "<html><body><h1>ABOUT</h1></body></html>", code = HTTP_OK_200, mime = MIME_HTML))
      case _ => OK(APIResponse(msg = "<html><body><h1>INFO</h1></body></html>", code = HTTP_OK_200, mime = MIME_HTML))
    }
  }
}