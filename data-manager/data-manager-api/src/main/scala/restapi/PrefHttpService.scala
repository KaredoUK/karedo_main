package restapi


import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import spray.routing._

//import spray.http.Uri.Path

// All APIs starting with /account go here
trait PrefHttpService


  extends HttpService
    with Directives
    with DefaultJsonFormats
    with ApiErrorsJsonProtocol
    with ApiDataJsonProtocol
    with CORSDirectives {

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  def routePref =
    pathPrefix("pref") {
      prefNames
    }

  def prefNames =
    path("names") {
      get {
        complete(
          List(

            ("IAB22", "offers & discounts"),
            ("IAB18", "fashion & style"),
            ("IAB8", "food & drink"),
            ("IAB20", "travel & holidays"),
            ("IAB17", "sports"),
            ("IAB6", "family & children"),
            ("IAB7", "health & fitness"),
            ("IAB19", "computers & gadgets"),
            ("IAB4", "jobs & career"),
            ("IAB10", "home & garden"),
            ("IAB2", "cars & bikes"),
            ("IAB13", "personal finance"),
            ("IAB3", "business & finance"),
            ("IAB1", "arts & entertainment"),
            ("IAB14", "community & society"),
            ("IAB15", "science"),
            ("IAB16", "pets"),
            ("IAB5", "education"),
            ("IAB21", "property & housing"),
            ("IAB9", "hobbies & interests"),
            ("IAB11", "law, govt & politics"),
            ("IAB12", "news & current affairs"),
            ("IAB23", "religion & spirituality")
          )
        )
      }
    }

}
