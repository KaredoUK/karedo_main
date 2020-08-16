package api



import akka.actor.ActorRef
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import com.parallelai.wallet.datamanager.data.BrandData
import com.parallelai.wallet.datamanager.data.BrandResponse
import core.BrandActor.{InternalBrandError, InvalidBrandRequest, BrandError}
import ApiDataJsonProtocol._
import spray.http.StatusCodes._
import spray.http._
import spray.routing.Directives

import scala.concurrent.ExecutionContext



class BrandService(brandActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {




  import akka.pattern.ask


import scala.concurrent.duration._
  implicit val timeout = Timeout(20.seconds)

  implicit object EitherErrorSelector extends ErrorSelector[BrandError] {
    def apply(error: BrandError): StatusCode = error match {
      case InvalidBrandRequest(reason) => BadRequest
      case InternalBrandError(_) => InternalServerError
    }
  }

  val route =
    path("brand") {
      post {

        handleWith {
          brandData: BrandData =>

              (brandActor ? brandData).mapTo[Either[BrandError,BrandResponse]]
              //UUIDData(UUID.randomUUID())

        }
      }
    }
}
