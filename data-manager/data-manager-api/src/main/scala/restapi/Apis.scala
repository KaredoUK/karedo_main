package restapi

import com.escalatesoft.subcut.inject.Injectable
import core.{ServiceActors, CoreActors, Core}
import akka.actor.{ActorRefFactory, Props}
import spray.routing.RouteConcatenation
import com.mongodb.casbah.commons.conversions.scala._

import scala.util.Try


/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.CoreActors``, which provides access
 * to the top-level actors that make up the system.
 */
trait Apis extends RouteConcatenation with Injectable {
  this: ServiceActors with Core  =>

  private implicit val _ = system.dispatcher

  private val bindPort = injectOptionalProperty[Int]("service.port") getOrElse 8080
  private val serviceURL = injectOptionalProperty[String]("service.url") getOrElse "localhost"
  private val doSwagger = injectOptionalProperty[String]("swagger").getOrElse("true").toBoolean


  val serveAccount = new AccountHttpService(registration, editAccount, brand, offer, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }
  val serveAccount2 = new AccountSuggestedOffersHttpService {
    override implicit def actorRefFactory: ActorRefFactory = system
  }

  val serveUser = new UserHttpService(registration, editAccount, brand, offer, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }

  
  val serveMedia = new MediaHttpService(media, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }

  val serveBrand = new BrandHttpService(brand, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }
  
  val serveOffer = new OfferHttpService(offer, userAuthentication) {
     override implicit def actorRefFactory: ActorRefFactory = system   
  }
  
  val serveSale = new SaleHttpService(offer, userAuthentication) {
     override implicit def actorRefFactory: ActorRefFactory = system   
  }

  val serveMerchant = new MerchantHttpService(offer, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }

  val routes =
    serveAccount.route ~
    serveAccount2.route ~
    serveAccount2.route2 ~
    serveUser.route ~
      serveBrand.route ~
      serveMedia.route ~
      serveOffer.route ~
      serveSale.route ~
      serveMerchant.route ~
      new MockService(other, userAuthentication).route

  val rootService = system.actorOf(Props(new RoutedHttpService(serviceURL, bindPort, routes,doSwagger)))

}
