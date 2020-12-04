package restapi

import com.escalatesoft.subcut.inject.Injectable
import core.{ServiceActors, Core}
import akka.actor.Props
import spray.routing.RouteConcatenation
import com.mongodb.casbah.commons.conversions.scala._
import com.gettyimages.spray.swagger.SwaggerHttpService
import scala.reflect.runtime.universe._
import com.parallelai.wallet.datamanager.data._


/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.CoreActors``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api extends RouteConcatenation {
  this: ServiceActors with Core with Injectable =>

  private implicit val _ = system.dispatcher

  private val bindPort = injectOptionalProperty[Int]("service.port") getOrElse 8080
  //val bindAddress = injectOptionalProperty[String]("service.bindAddress") getOrElse "0.0.0.0"

  val routes =
    new AccountService(registration, editAccount, userAuthentication).route ~
    new BrandService(brand, editAccount, userAuthentication).route ~
    new MediaService(media, userAuthentication).route ~
    new OfferService(offer, userAuthentication).route ~
    new MockService(other, userAuthentication).route  ~
      new SwaggerService(system).route  ~
      new SwaggerHttpService {
        def actorRefFactory = system
        def apiTypes = Seq( typeOf[AccountService] )
        def modelTypes =
          Seq(
            typeOf[RegistrationRequest]
            //,typeOf[RegistrationResponse]
            //,typeOf[RegistrationValidation]//,
            //            typeOf[RegistrationValidationResponse]
          )
        def apiVersion = "1.0"
        def baseUrl = s"http://localhost:$bindPort"
        def specPath = "api"
        def resourcePath = "api-docs"
      }.routes

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))



}
