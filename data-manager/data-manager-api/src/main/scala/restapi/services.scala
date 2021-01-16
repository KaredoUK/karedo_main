package restapi

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import core.security.UserAuthService
import spray.http.StatusCodes._
import spray.http._
import spray.routing._
import spray.routing.directives.{LogEntry, RouteDirectives}


import spray.util.{SprayActorLogging, LoggingContext}
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import spray.httpx.marshalling.{ToResponseMarshallingContext, Marshaller}
import spray.http.HttpHeaders.RawHeader
import akka.actor._
import scala.reflect.runtime.universe._

/**
 * Holds potential error response with the HTTP status and optional body
 *
 * @param responseStatus the status code
 * @param response the optional body
 */
case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends Exception

/**
 * Provides a hook to catch exceptions and rejections from routes, allowing custom
 * responses to be provided, logs to be captured, and potentially remedial actions.
 *
 * Note that this is not marshalled, but it is possible to do so allowing for a fully
 * JSON API (e.g. see how Foursquare do it).
 */
trait FailureHandling {
  this: HttpService =>

  // For Spray > 1.1-M7 use routeRouteResponse
  // see https://groups.google.com/d/topic/spray-user/zA_KR4OBs1I/discussion
  def rejectionHandler: RejectionHandler = RejectionHandler.Default

  def exceptionHandler(implicit log: LoggingContext) = ExceptionHandler {

    case e: IllegalArgumentException => ctx =>
      loggedFailureResponse(ctx, e,
        message = "The server was asked a question that didn't make sense: " + e.getMessage,
        error = NotAcceptable)

    case e: NoSuchElementException => ctx =>
      loggedFailureResponse(ctx, e,
        message = "The server is missing some information. Try again in a few moments.",
        error = NotFound)

    case t: Throwable => ctx =>
      // note that toString here may expose information and cause a security leak, so don't do it.
      loggedFailureResponse(ctx, t)
  }

  private def loggedFailureResponse(ctx: RequestContext,
                                    thrown: Throwable,
                                    message: String = "The server is having problems.",
                                    error: StatusCode = InternalServerError)
                                   (implicit log: LoggingContext): Unit = {
    log.error(thrown, ctx.request.toString)
    // Please ignore the warning about the tuple conversion (see https://groups.google.com/forum/#!msg/spray-user/klCO6y4Btmo/PBFXRswfjhoJ)
    ctx.complete(error, message)
  }

}

/**
 * Allows you to construct Spray ``HttpService`` from a concatenation of routes; and wires in the error handler.
 * It also logs all internal server errors using ``SprayActorLogging``.
 *
 */
class RoutedHttpService(serviceURL: String, bindPort: Int, routes: Route)
  extends Actor

  with HttpService
  with ActorLogging {

  override implicit def actorRefFactory = context


  //val bindAddress = injectOptionalProperty[String]("service.bindAddress") getOrElse "0.0.0.0"


  val swaggerRoutes = new SwaggerHttpService {
    def actorRefFactory = context

    def apiTypes = Seq(typeOf[AccountHttpService], typeOf[MediaHttpService], typeOf[BrandHttpService])

    def modelTypes =
      Seq(
        typeOf[RegistrationRequest]
        //,typeOf[RegistrationResponse]
        //,typeOf[RegistrationValidation]//,
        //            typeOf[RegistrationValidationResponse]
      )

    def apiVersion = "1.1"

    def baseUrl = s"http://$serviceURL:$bindPort"

    def specPath = "api"

    def resourcePath = "api-docs"
  }.routes ~ get {
    pathPrefix("swagger") {
      pathEndOrSingleSlash {
        getFromResource("swagger/index.html")
      }
    } ~
      getFromResourceDirectory("swagger")
  }


  implicit val handler = ExceptionHandler {
    case NonFatal(ErrorResponseException(statusCode, entity)) => ctx =>
      ctx.complete(statusCode, entity)

    case NonFatal(e) => ctx => {
      log.error(e, InternalServerError.defaultMessage)
      ctx.complete(InternalServerError)
    }
  }

  import akka.event.Logging.InfoLevel
  // logs just the request method and response status at info level
  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse =>
      Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status + " "+res.message, InfoLevel))
    case _ => None // other kind of responses
  }
  def routeWithLogging = logRequestResponse(requestMethodAndResponseStatusAsInfo _)(routes)


  def receive: Receive =
    runRoute(routeWithLogging ~ swaggerRoutes)
    (handler, RejectionHandler.Default, context, RoutingSettings.default, LoggingContext.fromActorRefFactory)


}

