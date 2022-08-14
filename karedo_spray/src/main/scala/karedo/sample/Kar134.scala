package karedo.sample

import java.util.UUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pakkio on 10/3/16.
  */
class Kar134 {
  def nl2br(s: String) = s.replace("\n", "<br>")
}

object Kar134 {
  val logger = LoggerFactory.getLogger(classOf[Kar134])

  def exec(accountId: UUID,
           deviceId: Option[String],
           applicationId: String,
           sessionId: String,
           adCount: Option[String]): ToResponseMarshallable = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nadCount: $adCount")
    "OK"
  }

  def route = {
    Route(

      // GET /account/{{account_id}}/ads?p={{application_id}}&s={{session_id}}&c={{ad_count}}


      path("account" / JavaUUID / "ads") {
        accountId =>
          optionalHeaderValueByName("X_Identification") { deviceId =>
            get {
              parameters('p, 's, 'c ?) {
                (applicationId, sessionId, adCount) => complete(Future {
                  Kar134.exec(accountId, deviceId, applicationId, sessionId, adCount)
                })

              }
            }
          }
      }
    )
  }
}
