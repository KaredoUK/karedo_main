import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import scala.concurrent.Future._

object Global extends GlobalSettings {

  // 500 - internal server error
  override def onError(request: RequestHeader, throwable: Throwable) = successful {
    InternalServerError(views.html.errors.onError(throwable))
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[SimpleResult] = successful{
    BadRequest(views.html.errors.onBadRequest(request, error))
  }

}