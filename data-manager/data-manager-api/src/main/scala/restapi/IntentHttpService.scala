package restapi


import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import spray.http.StatusCodes
import spray.routing._

//import spray.http.Uri.Path

// All APIs starting with /account go here
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
