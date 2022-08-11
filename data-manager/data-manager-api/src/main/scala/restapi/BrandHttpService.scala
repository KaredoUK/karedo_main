package restapi

import java.util.UUID

import akka.actor.{ActorRef}
import akka.util.Timeout
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse, ListBrandsAdverts, _}
import akka.pattern.ask
import core.security.UserAuthService
import core.{SuccessResponse, ResponseWithFailure}
import core.objAPI._
import spray.routing._

import scala.concurrent.{Future, ExecutionContext}

// All APIs starting with /brand go here
abstract class BrandHttpService(protected val brandActor: ActorRef,
                                override protected val userAuthService: UserAuthService)
                               (implicit protected val executionContext: ExecutionContext)
  extends HttpService
  with Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with ApiDataJsonProtocol
  with AuthorizationSupport {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(2.seconds)

  val route =
    pathPrefix("brand") {
      createBrand ~         // P67 POST AUTH /brand
        listBrands ~        // P95 GET AUTH /brand
        getBrand ~          // ????? missing? GET AUTH /brand/xxx
        deleteBrand ~       // P68 DELETE AUTH /brand/xxx
        listBrandsAdverts ~ // P64 GET AUTH /brand/xxx/advert
        addAdvert ~         // P65 POST AUTH /brand/xxx
        getAdvertSummary ~         // P61 GET AUTH /brand/xxx/advert/xxx/summary
        getAdvertDetail ~   // P126 get detail of offer
        deleteAdvert ~       // P66 DELETE AUTH /brand/xxx/advert/xxx
        addInteraction       // P108 add Interaction
    }

  def addInteraction =
      path("brand" / "interaction") {
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          post {
            handleWith {
              brandData: UserBrandInteraction =>
                Future[String]("")
            }
          }
        }
      }

  // P67 CREATE BRAND
  def createBrand =
    pathEnd {
      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        post {
          handleWith {
            brandData: BrandData =>
              (brandActor ? brandData).

                mapTo[ResponseWithFailure[APIError, BrandResponse]]
          }
        }
      }
    }

  // P95 LIST BRANDS

  def listBrands =
    pathEnd {
      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        get {
          rejectEmptyResponse {

            complete {
              (brandActor ? ListBrands).

                mapTo[List[BrandRecord]]
            }
          }
        }
      }
    }

  // ?????
  def getBrand = path(JavaUUID) { brandId: UUID =>
    userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
      rejectEmptyResponse {
        get {
          complete {

            (brandActor ? BrandIDRequest(brandId)).

              mapTo[ResponseWithFailure[APIError, BrandRecord]]
          }
        }

      }
    }
  }

  // P68 DEACTIVATE BRAND
  def deleteBrand = path(JavaUUID) { brandId: UUID =>
    userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
      delete {
        complete {
          (brandActor ? DeleteBrandRequest(brandId)).

            mapTo[ResponseWithFailure[APIError, StatusResponse]]
        }
      }
    }
  }

  // P64 LIST ADS PER BRAND
  def listBrandsAdverts =
    path(JavaUUID / "advert") {
      brandId: UUID =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          rejectEmptyResponse {
            get {
              complete {
                (brandActor ? ListBrandsAdverts(brandId)).mapTo[ResponseWithFailure[APIError, List[AdvertDetailListResponse]]]
              }
            }
          }
        }
    }


  // title("PARALLELAI-61API: Get Ad Details")
  lazy val getAdvertSummary : Route =
    path(JavaUUID / "advert" / JavaUUID / "summary") {
      (brand, advert) =>
      {
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          get {
            complete {
              (brandActor ? GetAdvertSummary(brand,advert)).

                mapTo[ResponseWithFailure[APIError,AdvertSummaryResponse]]
            }
          }
        }
      }
    }

  // title("PARALLELAI-61API: Get Ad Details")
  lazy val getAdvertDetail : Route =
    path(JavaUUID / "advert" / JavaUUID / "detail") {
      (brand, advert) =>
      {
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          get {
            complete {
              (brandActor ? GetAdvertDetail(brand,advert)).

                mapTo[ResponseWithFailure[APIError,AdvertDetailResponse]]
            }
          }
        }
      }
    }

  // PARALLELAI-65
  def addAdvert =
    path(JavaUUID / "advert") {
      brandId: UUID =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>

          post {
            handleWith {
              request: AdvertDetailApi => {
                (brandActor ? AddAdvertCommand(
                  brandId, request.shortText, request.detailedText, request.termsAndConditions, request.shareDetails,
                  request.summaryImages,
                  request.startDate, request.endDate, request.imageIds,
                  request.karedos)).

                  mapTo[ResponseWithFailure[APIError, AdvertDetailListResponse]]
              }
            }
          }
        }
    }

  // PARALLELAI-66 DISABLE AD
  def deleteAdvert =
    path(JavaUUID / "advert" / JavaUUID) {
      (brandId: UUID, advId: UUID) =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          delete {
            complete {
              (brandActor ? DeleteAdvRequest(brandId, advId)).mapTo[ResponseWithFailure[APIError, StatusResponse]]
            }
          }
        }
    }
}