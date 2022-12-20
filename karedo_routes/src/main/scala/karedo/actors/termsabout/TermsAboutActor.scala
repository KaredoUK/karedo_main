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


trait TermsAboutActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[TermsAboutActor])

  def exec(termsAbout: String): Result[Error, APIResponse] = {
    val msg: String = termsAbout match {
      case GET_TERM => terms.html.terms.render().toString
      case GET_ABOUT  => terms.html.about.render().toString
      case GET_PRIVACY  => terms.html.privacy.render().toString
      case _ => "<html><body><h1>INFO</h1></body></html>"
    }

    OK(APIResponse(msg = msg, code = HTTP_OK_200, mime = MIME_HTML))
  }
}