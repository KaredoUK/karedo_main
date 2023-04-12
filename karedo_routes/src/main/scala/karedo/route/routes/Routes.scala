package karedo.route.routes

import karedo.common.cors.CorsSupport
import karedo.route.routes.ads._
import karedo.route.routes.inform.post_InformRoute
import karedo.route.routes.intent._
import karedo.route.routes.login._
import karedo.route.routes.prefs._
import karedo.route.routes.profile._
import karedo.route.routes.sale._
import karedo.route.routes.termsabout._
import karedo.route.routes.transfer._
import karedo.route.routes.url._
import karedo.route.sample.Entities
import karedo.route.util.RouteDebug
//import org.clapper.classutil.ClassInfo
import org.slf4j.LoggerFactory

// For CORS
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpHeader, HttpResponse}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, MethodRejection, RejectionHandler}

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
//    val classesInfos = ClassFinder.concreteSubclasses("karedo.route.routes.KaredoRoute",classes)
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

