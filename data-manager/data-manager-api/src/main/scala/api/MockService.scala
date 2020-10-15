package api

import java.util.UUID

import akka.actor.ActorRef
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{InteractionType, InteractionResponse}
import core.OtherActor._
import core.ResponseWithFailure
import shapeless.HNil
import spray.routing.{Route, Directive, Directives}
import akka.pattern.ask

import scala.concurrent.ExecutionContext


object MockService {
  val logger = Logger("OtherService")
}

class MockService(otherActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol {

  import scala.concurrent.duration._

  val q="\""

  implicit val timeout = Timeout(20.seconds)

  val route55: Route =

  // PARALLELAI-55API: User Brand Interaction
  // "user/"+userId+"/interaction/brand/"+brandId, { "interactionType":  "BUY"}
    path("user" / JavaUUID / "interaction" / "brand" / JavaUUID)
    { (user, brand) => {
      /*get {
        complete {
          """{"a":"5"}"""
        }
      }
    } ~ */
      post {
        handleWith( (s:String) =>

          s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}")
        }
      }

    }
  val route56: Route =
    // PARALLELAI-56API: User Ads Interaction")
    // r = post("user/"+userId+"/interaction/advert/"+advertId)
    path("user" / JavaUUID / "interaction" / "advert" / JavaUUID)
    {
      (user, advert) => {
        post {
          handleWith( (s:String) =>
            s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}"
          )
        }
      }
    }

  //title("PARALLELAI-59API: Get Next N Ads For User For Brand")
  //r = get("account/"+userId+"/brand/"+brandId+"/ads?max=5")
  val route59: Route =
    path("account" / JavaUUID / "brand" / JavaUUID / "ads") {
      (user, brand) => {

        get {
          parameters('max) { max =>
            rejectEmptyResponse {
              complete {
                """[
                  |{"id":"f135e16d-da0c-4b12-ab7f-d19ae1c7abe3","name":"name1","iconId":"5678666666"},
                  |{"id":"f135e16d-da0c-4b12-ab7f-d19ae1c7abe3","name":"name2","iconId":"5678666667"}
                  |]
                """.stripMargin
              }
            }
          }
        }
      }
    }

 // title("PARALLELAI-61API: Get Ad Details")

 //  r = get("brand/"+brandId+"/advert/"+advertId)
  val route61 : Route =
    path("brand" / JavaUUID / "advert" / JavaUUID ){
      (brand,advert) => {
        get {
          complete {
            """
              |{ "title":"title1", "text":"text1", "imageIds":"image1,image2,image3" }
            """.stripMargin
          }
        }
      }
    }

  // title("PARALLELAI-63API: User Buy Offer")
  // r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "BUY"})
  val route63 : Route =
    path("user"/ JavaUUID / "interaction" / "offer" / JavaUUID ){
      (user,offer) => {
        post {
          handleWith (
            (s:String) =>
              s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}")

        }
      }
    }

  // title("PARALLELAI-71API: Remove User Brand")
  // r = delete("account/"+userId+"/brand/"+brandId)
  val route71 : Route =
    path("account" / JavaUUID / "brand" / JavaUUID) {
      (user,brand) => {
        delete {
          complete {
            "{}"
          }
        }
      }
    }

  // title("PARALLELAI-79API: Show Pending Ads Per User Per Brand")
  // r = get("account/"+userId+"/brand/"+brandId+"/pendingAds")
  val route79 : Route =
    path("account" / JavaUUID / "brand" / JavaUUID / "pendingAds") {
      (user, brand) => {
        get {
          complete {
            """[
              |{"id":"f135e16d-da0c-4b12-ab7f-d19ae1c7abe3","text":"name1","imageId":"5678666666","value":"0.5555"},
              |{"id":"f135e16d-da0c-4b12-ab7f-d19ae1c7abe3","text":"name2","imageId":"5678666667","value":"0.7777"}
              |]
            """.stripMargin
          }
        }
      }
    }

  // title("PARALLELAI-80API: List Offers For User")
  // r = get("user/"+userId+"/recommendedOffers?start=0&maxCount=5")
  val route80 : Route =
    path("user" / JavaUUID / "recommendedOffers" ){
      (user) => {
        get {

          parameters('start, 'maxCount) { (start, maxCount) =>
            rejectEmptyResponse {
              complete {
                """[
                  |{"name":"offername","brandId":"f135e16d-da0c-4b12-ab7f-d19ae1c7abe3","desc":"description","imageId":"imageid1","qrCodeId":"qrCodeId","value":"0.5555"},
                  |{"name":"offername2","brandId":"f135e16d-da0c-4b12-ab7f-d19ae1c7abe3","desc":"description2","imageId":"imageid1","qrCodeId":"qrCodeId","value":"0.5555"}
                  |]
                """.stripMargin

              }
            }
          }
        }
      }
    }

  //         title("PARALLELAI-81API: User Offer Interaction (like-dislike-share)")
  // r = post("user/"+userId+"/interaction/offer/"+offerId, { "interactionType":  "LIKE"})
  val route81 : Route =
    path("user" / JavaUUID / "interaction" / "offer" / JavaUUID ) {
      (user,offer) => {
        post {
          handleWith {
            (s:String) =>
              s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}"
          }
        }
      }
    }
  // title("PARALLELAI-82API: Get Offer Details")
  // r = get("offer/"+offerId)
  val route82 : Route =
    path("offer" / JavaUUID ) {
      (offer) => {
        get {
          complete {
            """
              | {"name":"offername","brandId":"f135e16d-da0c-4b12-ab7f-d19ae1c7abe3","desc":"description","imageId":"imageid1","qrCodeId":"qrCodeId","value":"0.5555"}
            """.stripMargin
            //"name: %s, brandId: %s, desc: %s, imageId: %s, qrCodeId: %s, value: %s"
          }
        }
      }
    }

  //         title("PARALLELAI-92API: Disable Offer")
  // r = delete("offer/"+offerId)
  val route92 : Route =
    path("offer" / JavaUUID) {
      (offer) => {
        delete {
          complete {
            "{}"
          }
        }
      }
    }


  val route=route55 ~ route56 ~ /* 57 */ route59 ~ route61 ~
    route63 ~ route71 ~ route79 ~ route80 ~ route81 ~ route82 ~ route92

}
