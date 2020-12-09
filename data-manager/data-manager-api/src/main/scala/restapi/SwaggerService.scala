package restapi

import akka.actor.ActorRefFactory
import spray.routing.Directives

class SwaggerService(actorContext: ActorRefFactory) extends Directives with DefaultJsonFormats {

  implicit val _ = actorContext

  val route =
    path("swagger") {
      getFromResource("swagger/index.html")
    } ~
    getFromResourceDirectory("swagger")

}