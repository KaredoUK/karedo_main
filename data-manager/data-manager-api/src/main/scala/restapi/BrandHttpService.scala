package restapi

import java.util.UUID

import akka.actor.{ActorRef}
import akka.util.Timeout
import com.wordnik.swagger.annotations.{Api => ApiDoc, _}
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse, ListBrandsAdverts, _}
import akka.pattern.ask
import core.security.UserAuthService
import core.{SuccessResponse, ResponseWithFailure}
import core.objAPI._
import spray.routing._

import scala.concurrent.ExecutionContext

// All APIs starting with /brand go here
@ApiDoc(value = "/brand", description = "Operations on the brand.", position = 1)
abstract class BrandHttpService(protected val brandActor: ActorRef,
                    override protected val userAuthService: UserAuthService)
                  (implicit protected val executionContext: ExecutionContext)
  extends HttpService
  with CreateBrand
  with ListBrands
  with GetBrand
  with DeleteBrand
  with ListBrandsAdverts
  with AddAdvert
  with DeleteAdvert
{

  val route =
    pathPrefix("brand") {
      createBrand ~
      listBrands ~
      getBrand ~
      deleteBrand ~
      listBrandsAdverts ~
      addAdvert ~
      deleteAdvert

    }
}
trait BrandServiceActorComponent
  extends Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with ApiDataJsonProtocol
  with AuthorizationSupport {

  protected def brandActor: ActorRef
  protected implicit val executionContext: ExecutionContext

  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
}

trait CreateBrand extends BrandServiceActorComponent {
  // dealing with /brand to create inquiry a brand
  def createBrand =
    pathEnd {
      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        post {
          handleWith {
            brandData: BrandData =>
              (brandActor ? brandData).mapTo[ResponseWithFailure[APIError, BrandResponse]]
          }
        }
      }
    }
}
trait ListBrands extends BrandServiceActorComponent {
  def listBrands =
    pathEnd {
      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        get {
          rejectEmptyResponse {

            complete {
              (brandActor ? ListBrands).mapTo[List[BrandRecord]]
            }
          }
        }
      }
    }
}

trait GetBrand extends BrandServiceActorComponent {

  def getBrand = path(JavaUUID) { brandId: UUID =>
    userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
      rejectEmptyResponse {
        get {
          complete {

            (brandActor ? BrandIDRequest(brandId)).mapTo[ResponseWithFailure[APIError, BrandRecord]]
          }
        }

      }
    }
  }
}

trait DeleteBrand extends BrandServiceActorComponent {
  def deleteBrand = path(JavaUUID) { brandId: UUID =>
    userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
      delete {
        complete {
          (brandActor ? DeleteBrandRequest(brandId)).mapTo[ResponseWithFailure[APIError, String]]
        }
      }
    }
  }
}
trait ListBrandsAdverts extends BrandServiceActorComponent {
  def listBrandsAdverts =
    path(JavaUUID / "advert") {
      brandId: UUID =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          rejectEmptyResponse {
            get {
              complete {
                (brandActor ? ListBrandsAdverts(brandId)).mapTo[ResponseWithFailure[APIError, List[AdvertDetailResponse]]]
              }
            }
          }
        }
    }
}
trait AddAdvert extends BrandServiceActorComponent {
  def addAdvert =
    path(JavaUUID / "advert") {
      brandId: UUID =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>

          post {
            handleWith {
              request: AdvertDetail => {
                (brandActor ? AddAdvertCommand(brandId, request.text, request.imageIds, request.value)).mapTo[ResponseWithFailure[APIError, AdvertDetailResponse]]
              }
            }
          }
        }
    }
}
trait DeleteAdvert extends BrandServiceActorComponent {
  def deleteAdvert =
    path(JavaUUID / "advert" / JavaUUID) {
      (brandId: UUID, advId: UUID) =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          delete {
            complete {
              (brandActor ? DeleteAdvRequest(brandId, advId)).mapTo[ResponseWithFailure[APIError, String]]
            }
          }
        }
    }
}