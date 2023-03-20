package karedo.routes.sale

import java.io.File
import java.nio.file.{Files, Paths}

import akka.http.scaladsl.server.Directives._
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.routes.KaredoRoute
import karedo.util._
import org.slf4j.LoggerFactory

/**
  * Created by charaj on 17/04/2017.
  */
object get_ImageRoute extends KaredoRoute with get_ImageActor
{
  def route = {
    path("image" / Segment) {
      imageName =>
        get {
          doCall {
            exec(imageName)
          }
        }
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
