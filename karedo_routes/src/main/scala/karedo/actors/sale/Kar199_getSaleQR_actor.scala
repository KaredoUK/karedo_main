package karedo.actors.sale

import java.io.File

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
  with KaredoConstants
  with KaredoQRCode {
  override val logger = LoggerFactory.getLogger(classOf[Kar199_getSaleQR_actor])

  def exec(saleId: String): Result[Error, APIResponse] = {
    getQRCode(saleId) match {
      case OK(s) => OK(APIResponse(Kar199Res(s).toJson.toString, HTTP_OK_200))
      case KO(e) => KO(e)
    }
  }
}

trait Kar199_postSaleQR_actor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoQRCode {
  override val logger = LoggerFactory.getLogger(classOf[Kar199_getSaleQR_actor])

  def exec(imageFile: File): Result[Error, APIResponse] = {
    decodeQRCode(imageFile) match {
      case OK(s) => OK(APIResponse(Kar199Res(s).toJson.toString, HTTP_OK_200))
      case KO(e) => KO(e)
    }
  }
}