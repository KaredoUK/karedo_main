package api

import core.{CoreActors, Core}
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
  this: CoreActors with Core =>

  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()

  private implicit val _ = system.dispatcher

  val routes =
    new AccountService(registration, editAccount).route

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))

}
