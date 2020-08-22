package api



import akka.actor.ActorRef
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import com.parallelai.wallet.datamanager.data.BrandData
import com.parallelai.wallet.datamanager.data.BrandResponse
import core.BrandActor.{InternalBrandError, InvalidBrandRequest, BrandError}
import ApiDataJsonProtocol._
import core.{SuccessResponse, FailureResponse, ResponseWithFailure}
import parallelai.wallet.entity.Brand
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.marshalling.{CollectingMarshallingContext, Marshaller}
import spray.routing.Directives

import scala.concurrent.ExecutionContext



class BrandService(brandActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol {


  import akka.pattern.ask
 import scala.concurrent.duration._
  implicit val timeout = Timeout(20.seconds)



  val route =
    path("brand") {

      rejectEmptyResponse {
        get {
          complete {

            (brandActor ? ListBrands).mapTo[List[BrandRecord]]
          }
        }
      } ~
      post {
        handleWith {
          brandData: BrandData =>
              (brandActor ? brandData).mapTo[ResponseWithFailure[BrandError,BrandResponse]]
        }
      }

    }
}
