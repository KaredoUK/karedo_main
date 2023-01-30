package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.routes.ads._
import karedo.routes.inform.post_InformRoute
import karedo.routes.intent._
import karedo.routes.login._
import karedo.routes.prefs._
import karedo.routes.profile._
import karedo.routes.sale._
import karedo.routes.transfer._
import karedo.routes.url._
import karedo.routes.termsabout._
import karedo.sample.Entities
import karedo.util.RouteDebug
//import org.clapper.classutil.ClassInfo
import org.slf4j.LoggerFactory

// For CORS
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Credentials`
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Methods`
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Origin`
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Headers`
import akka.http.scaladsl.model.headers.`Access-Control-Max-Age`
import akka.http.scaladsl.model.headers.Origin
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.server.RejectionHandler

trait Routes
  extends Entities
    with RouteDebug
  with CorsSupport {

  override val routes = cors {
    // Ads
    get_AdsRoute.route ~ get_PointsRoute.route ~ get_MessagesRoute.route ~
    // intent
      get_IntentRoute.route ~ post_IntentRoute.route ~ put_IntentRoute.route ~ delete_IntentRoute.route ~
    // login
      post_LoginRoute.route ~ post_SendCodeRoute.route ~ get_VerifyEmailRoute.route ~ post_EnterCodeRoute.route ~ put_ResendRoute.route ~
      put_ResendEmailRoute.route ~ post_ValidateEmailRoute.route ~ delete_AccountRoute.route ~ get_ValidateSessionRoute.route ~
    // profile
      get_ProfileRoute.route ~ post_ProfileRoute.route ~ post_ChangePasswordRoute.route ~
    // prefs
      get_PrefsRoute.route ~ post_PrefsRoute.route ~
    // Sale
      put_TransferRoute.route ~ put_SaleRoute.route ~ get_SaleRoute.route ~ post_SaleRoute.route ~
      get_SaleQRRoute.route ~ post_QRFileRoute.route ~
    // Interaction et al
      get_FavouriteRoute.route ~ post_InteractionRoute.route ~ post_FavouriteRoute.route ~
      post_ShareDataRoute.route ~
    // Url Magic
      get_UrlMagicNormalRoute.route ~ get_UrlMagicShareRoute.route ~
    // Terms and About
      get_TermsRoute.route ~ get_AboutRoute.route ~ get_PrivacyRoute.route ~
    // Images
      get_ImageRoute.route ~
    // Base
      get_BaseRoute.route ~
    // Inform
      post_InformRoute.route
  }

//  override val routes = {
//    println("findAllRoutesExtendingKaredoRoute")
//    def companion[T](name : String)(implicit man: Manifest[T]) : T =
//      Class.forName(name).getField("MODULE$").get(null).asInstanceOf[T]
//
//    import org.clapper.classutil.ClassFinder
//    val rootRoute: Route = path("") {
//      get(complete("Karedo API version 0.0.2-SNAPSHOT"))
//    }
//    val finder = ClassFinder()
//    val classes = finder.getClasses()
//
//    val classesInfos = ClassFinder.concreteSubclasses("karedo.routes.KaredoRoute",classes)
//
//    classesInfos.foreach(println)
//
//    val routes =  classesInfos.toList map { x:ClassInfo =>
//      val obj:KaredoRoute = companion[KaredoRoute](x.name)
//      obj.route
//    }
//    val newRoutes = routes.foldLeft(rootRoute)(_~_)
//
//    println(newRoutes)
//
//    newRoutes
//
//  }

  override val logger = LoggerFactory.getLogger(classOf[Routes])

}

trait CorsSupport {

  protected def corsAllowOrigins: List[String] = List("*")

  protected def corsAllowedHeaders: List[String]
    = List("Origin", "X-Requested-With", "Content-Type", "Accept", "Accept-Encoding", "Accept-Language",
    "Host", "Referer", "User-Agent", "Geolocation", "Authorization", "X-Authorization")

  protected def corsAllowCredentials: Boolean = true

  protected def optionsCorsHeaders: List[HttpHeader] = List[HttpHeader](
    `Access-Control-Allow-Headers`(corsAllowedHeaders.mkString(", ")),
    `Access-Control-Max-Age`(60 * 60 * 24 * 20), // cache pre-flight response for 20 days
    `Access-Control-Allow-Credentials`(corsAllowCredentials)
    )

  protected def corsRejectionHandler(allowOrigin: `Access-Control-Allow-Origin`) = RejectionHandler
    .newBuilder().handle {
    case MethodRejection(supported) =>
      complete(HttpResponse().withHeaders(
        `Access-Control-Allow-Methods`(OPTIONS, supported) ::
          allowOrigin ::
          optionsCorsHeaders
      ))
  }
    .result()

  private def originToAllowOrigin(origin: Origin): Option[`Access-Control-Allow-Origin`] =
    if (corsAllowOrigins.contains("*") || corsAllowOrigins.contains(origin.value))
      origin.origins.headOption.map(`Access-Control-Allow-Origin`.apply)
    else
      None

  def cors[T]: Directive0 = mapInnerRoute { route => context =>
    ((context.request.method, context.request.header[Origin].flatMap(originToAllowOrigin)) match {
      case (OPTIONS, Some(allowOrigin)) =>
        handleRejections(corsRejectionHandler(allowOrigin)) {
          respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials)) {
            route
          }
        }
      case (_, Some(allowOrigin)) =>
        respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials)) {
          route
        }
      case (_, _) =>
        route
    })(context)
  }
}