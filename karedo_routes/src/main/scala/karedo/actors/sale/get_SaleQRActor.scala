package karedo.actors.sale

import java.io.{ByteArrayInputStream, File, FileInputStream}
import java.nio.file.{Files, Paths}

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp}
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait get_SaleQRActor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with KaredoQRCode {
  override val logger = LoggerFactory.getLogger(classOf[get_SaleQRActor])

  def exec(saleId: String): Result[Error, APIResponse] = {
    getQRCode(saleId) match {
      case OK(s) => OK(APIResponse(QRCodeResponse(s).toJson.toString, HTTP_OK_200))
      case KO(e) => KO(e)
    }
  }
}

trait post_SaleQRActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoQRCode {
  override val logger = LoggerFactory.getLogger(classOf[get_SaleQRActor])

  def exec(imageFile: File): Result[Error, APIResponse] = {
    decodeQRCode(imageFile) match {
      case OK(s) => OK(APIResponse(QRCodeResponse(s).toJson.toString, HTTP_OK_200))
      case KO(e) => KO(e)
    }
  }
}

trait get_ImageActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoQRCode {
  override val logger = LoggerFactory.getLogger(classOf[get_ImageActor])

  def exec(imageName: String): Result[Error, APIResponse] = {
    try {
      val basePath = qr_base_file_path + File.separator + qr_img_file_path
      val imagePath = basePath + File.separator + imageName

      val bytes = Files.readAllBytes(Paths.get(imagePath))

      OK(APIResponse(msg = "", code = HTTP_OK_200, mime = MIME_PNG, bytes = bytes))

    } catch {
      case e:Exception => {
        logger.error("Getting image failed", e)
        KO(Error("Couldn't get image: " + e.toString))
      }
    }
  }
}

