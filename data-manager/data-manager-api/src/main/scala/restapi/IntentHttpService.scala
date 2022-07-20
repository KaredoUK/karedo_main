package restapi

import javax.ws.rs.Path

import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import com.wordnik.swagger.annotations.{ApiImplicitParams, ApiOperation, ApiResponses, _}
import spray.http.StatusCodes
import spray.routing._

//import spray.http.Uri.Path

// All APIs starting with /account go here
@Api(position = 1, value = "/intent", description = "Operations on the intent")
trait IntentHttpService


  extends HttpService
    with Directives
    with DefaultJsonFormats
    with ApiErrorsJsonProtocol
    with ApiDataJsonProtocol
    with CORSDirectives {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  def routeIntent =
    pathPrefix("intent") {
      intentWhat
    }

  @Path("/intent/what")
  @ApiOperation(position = 3, httpMethod = "GET", response = classOf[List[String]], value = "KAR-129 intent/what")
  @ApiImplicitParams(Array())
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")))
  def intentWhat =
    path("what") {
      get {
        complete(List(
          "buy",
          "rent",
          "travel",
          "hire",
          "compare",
          "switch",
          "borrow",
          "visit"

        ))
      }
    }

}
