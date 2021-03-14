package restapi

import java.util.UUID
import javax.ws.rs.Path

import akka.actor.{ActorRef}
import akka.util.Timeout
import com.wordnik.swagger.annotations._
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse, ListBrandsAdverts, _}
import akka.pattern.ask
import core.security.UserAuthService
import core.{SuccessResponse, ResponseWithFailure}
import core.objAPI._
import spray.routing._

import scala.concurrent.{Future, ExecutionContext}

// All APIs starting with /brand go here
@Api(position=2,value = "/brand", description = "Brands")
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
  @ApiOperation(position=1,httpMethod = "POST", response = classOf[BrandResponse], value = "Parallelai-67: Create a new Brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "request", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.BrandData", paramType = "body",
      value = "Details of the request"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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

  @ApiOperation(position=2,httpMethod = "GET", response = classOf[List[BrandRecord]],
    value = "Parallelai-95: List Brands")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @ApiOperation(position=3,httpMethod = "GET", response = classOf[BrandRecord],
    value = "Parallelai-XXX: Fetch a single Brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @ApiOperation(position=4,httpMethod = "DELETE", response = classOf[StatusResponse],
    value = "Parallelai-68: Deactivate Brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to deactivate"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @Path("/{brandId}/advert")
  @ApiOperation(position=5,httpMethod = "GET", response = classOf[List[AdvertDetailListResponse]],
    value = "Parallelai-64: List Ads per Brand...")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to search for ads"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
  def listBrandsAdverts =
    path(JavaUUID / "advert") {
      brandId: UUID =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
          rejectEmptyResponse {
            get {
              complete {
                (brandActor ? ListBrandsAdverts(brandId)).

                  mapTo[ResponseWithFailure[APIError, List[AdvertDetailListResponse]]]
              }
            }
          }
        }
    }


  // title("PARALLELAI-61API: Get Ad Details")
  @Path("/{brand}/advert/{adId}/summary")
  @ApiOperation(position=6,httpMethod = "GET", response = classOf[AdvertSummaryResponse],
    value = "Parallelai-61/ P125: Get Specific Ad per Brand / summary")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand "),
    new ApiImplicitParam(name = "adId", required = true, dataType = "String", paramType = "path",
      value = "UUID of ad"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @Path("/{brand}/advert/{adId}/detail")
  @ApiOperation(position=6,httpMethod = "GET", response = classOf[AdvertDetailResponse],
    value = "Parallelai-61/ P126: Get Specific Ad per Brand / detail")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand "),
    new ApiImplicitParam(name = "adId", required = true, dataType = "String", paramType = "path",
      value = "UUID of ad"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @Path("/{brandId}/advert")
  @ApiOperation(position=7,httpMethod = "POST", response = classOf[AdvertDetailListResponse],
    value = "Parallelai-65: Create Ad")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "Advert data",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.AdvertDetail",
      paramType = "body",
      value = "Advert to add"),
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to owning this new ad"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @ApiOperation(position=8,httpMethod = "DELETE", response = classOf[StatusResponse],
    value = "Parallelai-66: Disable ad")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to deactivate"),
    new ApiImplicitParam(name = "ad id", required = true, dataType = "String", paramType = "path",
      value = "UUID of ad to deactivate"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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