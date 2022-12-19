import restapi.AuthorizedFilter
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.config.AppConfigPropertySource
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import scala.concurrent.Future._

object Global extends WithFilters(AuthorizedFilter( newBindingModuleWithConfig(AppConfigPropertySource()) )) {

  // 500 - internal server error
  override def onError(request: RequestHeader, throwable: Throwable) = successful {
    InternalServerError(views.html.errors.onError(throwable))
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = successful{
    BadRequest(views.html.errors.onBadRequest(request, error))
  }

}