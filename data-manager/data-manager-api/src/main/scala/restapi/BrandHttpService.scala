package restapi

import java.util.UUID
import javax.ws.rs.Path

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
        deleteAdvert        // P66 DELETE AUTH /brand/xxx/advert/xxx

    }

  // P67 CREATE BRAND
  @ApiOperation(httpMethod = "POST", response = classOf[BrandResponse], value = "Parallelai-67: Create a new Brand")
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
              (brandActor ? brandData).mapTo[ResponseWithFailure[APIError, BrandResponse]]
          }
        }
      }
    }

  // P95 LIST BRANDS

  @ApiOperation(httpMethod = "GET", response = classOf[List[BrandRecord]],
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
              (brandActor ? ListBrands).mapTo[List[BrandRecord]]
            }
          }
        }
      }
    }

  // ?????
  @ApiOperation(httpMethod = "GET", response = classOf[BrandRecord],
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

            (brandActor ? BrandIDRequest(brandId)).mapTo[ResponseWithFailure[APIError, BrandRecord]]
          }
        }

      }
    }
  }

  // P68 DEACTIVATE BRAND
  @ApiOperation(httpMethod = "DELETE", response = classOf[String],
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
          (brandActor ? DeleteBrandRequest(brandId)).mapTo[ResponseWithFailure[APIError, String]]
        }
      }
    }
  }

  // P64 LIST ADS PER BRAND
  @ApiOperation(httpMethod = "GET", response = classOf[List[BrandRecord]],
    value = "Parallelai-64: List Ads per Brand")
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
                (brandActor ? ListBrandsAdverts(brandId)).mapTo[ResponseWithFailure[APIError, List[AdvertDetailResponse]]]
              }
            }
          }
        }
    }

  // PARALLELAI-65 CREATE AD
  @ApiOperation(httpMethod = "POST", response = classOf[AdvertDetailResponse],
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
              request: AdvertDetail => {
                (brandActor ? AddAdvertCommand(brandId, request.text, request.imageIds, request.value)).mapTo[ResponseWithFailure[APIError, AdvertDetailResponse]]
              }
            }
          }
        }
    }

  // PARALLELAI-66 DISABLE AD
  @ApiOperation(httpMethod = "DELETE", response = classOf[String],
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
              (brandActor ? DeleteAdvRequest(brandId, advId)).mapTo[ResponseWithFailure[APIError, String]]
            }
          }
        }
    }
}