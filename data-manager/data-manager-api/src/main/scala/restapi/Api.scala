package restapi

import core.{ServiceActors, CoreActors, Core}
import akka.actor.Props
import spray.routing.RouteConcatenation
import com.mongodb.casbah.commons.conversions.scala._

/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.CoreActors``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api extends RouteConcatenation {
  this: ServiceActors with Core =>

  private implicit val _ = system.dispatcher

  val routes =
    new AccountService(registration, editAccount, userAuthentication).route ~
    new BrandService(brand, editAccount).route ~
    new MediaService(media).route ~
    new OfferService(offer).route ~
    new MockService(other).route

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))

}
