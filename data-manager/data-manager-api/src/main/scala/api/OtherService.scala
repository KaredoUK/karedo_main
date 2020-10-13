package api

import java.util.UUID

import akka.actor.ActorRef
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{InteractionType, InteractionResponse}
import core.OtherActor._
import core.ResponseWithFailure
import shapeless.HNil
import spray.routing.{Route, Directive, Directives}
import akka.pattern.ask

import scala.concurrent.ExecutionContext


object OtherService {
  val logger = Logger("OtherService")
}

class OtherService(otherActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route: Route =

    path("user" / JavaUUID / "interaction/brand" / JavaUUID) {
      (brandId: UUID, advId: UUID) => {


        post {
          handleWith {
            interactionType: InteractionType =>
              (otherActor ? interactionType).mapTo[InteractionResponse]
          }
        } ~
        get {
          rejectEmptyResponse {

            complete {


              (otherActor ? "").mapTo[InteractionResponse]
            }
          }
        }

      }
    }

}
