package karedo.actors.sale

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp}
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar199_getSaleQR_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar199_getSaleQR_actor])

  def exec(saleId: String): Result[Error, APIResponse] = {
    OK(APIResponse(getQRForSale(saleId).toJson.toString, HTTP_OK_200))
  }

  def getQRForSale(saleId: String): Kar199Res = {
    val qrFileName = "qr.png"

    Kar199Res(s"${qr_base_url}/${qrFileName}")
  }
}