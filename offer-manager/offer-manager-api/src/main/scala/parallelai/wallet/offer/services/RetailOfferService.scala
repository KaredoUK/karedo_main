package parallelai.wallet.offer.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.routing.Directives
import api.DefaultJsonFormats
import parallelai.wallet.offer.actors.RetailOfferActor._
import akka.pattern._
import parallelai.wallet.entity.RetailOffer
import core.RegistrationActor.NotRegistered
import spray.http.{StatusCodes, StatusCode}
import spray.json.{JsString, JsValue, RootJsonFormat}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat


/**
 * Created by crajah on 27/04/2014.
 */
class RetailOfferService (retailOfferActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import scala.concurrent.duration._
  import akka.util.Timeout
  implicit val timeout = Timeout(2.seconds)

  implicit object jodaDateTimeFormat extends RootJsonFormat[DateTime] {
    //2013-12-17T13:46:19Z
    val DATE_FORMAT = ISODateTimeFormat.dateTimeParser()

    def write(obj: DateTime): JsValue = JsString(DATE_FORMAT.print(obj))

    def read(json: JsValue): DateTime = {
      json match {
        case JsString(date) => DATE_FORMAT.parseDateTime(date)

        case _ => throw new IllegalArgumentException(s"Expected JsString with content having format $DATE_FORMAT for a DateTime attribute value")
      }
    }
  }

  implicit object EitherErrorSelector extends ErrorSelector[String] {
    def apply(v: String): StatusCode = StatusCodes.BadRequest
  }

  implicit val retailOfferJson = jsonFormat8(RetailOffer)

  val route =
    path("offer") {
      get {
        complete {
          (retailOfferActor ? In ).mapTo[Either[String,Seq[RetailOffer]]]
        }
      } ~
      post {
        handleWith {
          in: RetailOffer => (retailOfferActor ? in).mapTo[Either[String,String]]
        }
      }
    }
}
