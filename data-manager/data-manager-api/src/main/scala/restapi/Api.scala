package restapi

import com.escalatesoft.subcut.inject.Injectable
import core.{ServiceActors, CoreActors, Core}
import akka.actor.{ActorRefFactory, Props}
import spray.routing.RouteConcatenation
import com.mongodb.casbah.commons.conversions.scala._


/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.CoreActors``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api extends RouteConcatenation with Injectable {
  this: ServiceActors with Core  =>

  private implicit val _ = system.dispatcher

  private val bindPort = injectOptionalProperty[Int]("service.port") getOrElse 8080
  private val serviceURL = injectOptionalProperty[String]("service.url") getOrElse "localhost"


  val serveAccount = new AccountHttpService(registration, editAccount, brand, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }
  val serveMedia = new MediaHttpService(media, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }

  val serveBrand = new BrandHttpService(brand, userAuthentication) {
    override implicit def actorRefFactory: ActorRefFactory = system
  }


  val routes =
    serveAccount.route ~
      serveBrand.route ~
      serveMedia.route ~
      new OfferService(offer, userAuthentication).route ~
      new MockService(other, userAuthentication).route

  println("%%%%%%%%%%%%%%serviceURL: $serviceURL");
  val rootService = system.actorOf(Props(new RoutedHttpService(serviceURL, bindPort, routes)))

}
