package karedo.route.routes.sale

import java.io.File

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory

/**
  * Created by charaj on 17/04/2017.
  */
object post_QRFileRoute extends KaredoRoute
  with post_SaleQRActor {

  //  val multipartForm =
  //    Multipart.FormData(Multipart.FormData.BodyPart.Strict(
  //      "csv",
  //      HttpEntity(ContentTypes.`text/plain(UTF-8)`, "2,3,5\n7,11,13,17,23\n29,31,37\n"),
  //      Map("filename" -> "primes.csv")))

  def route = {
    Route {
      path( "qr" ) {
        post {
          uploadedFile("file") {
            case (fileInfo, file) => {
              doCall({
                exec(file)
              })
            }
          }
        }
      }
    }
  }

  //  def routeNew =
  //    Route {
  //      path("qr") {
  //        entity(as[Multipart.FormData]) { formData =>
  //
  //          // collect all parts of the multipart as it arrives into a map
  //          val allPartsF: Future[Map[String, Any]] = formData.parts.mapAsync[(String, Any)](1) {
  //
  //            case b: BodyPart if b.name == "file" =>
  //              // stream into a file as the chunks of it arrives and return a future
  //              // file to where it got stored
  //              val file = File.createTempFile("upload", "tmp")
  //              b.entity.dataBytes.runWith(FileIO.toFile(file)).map(_ =>
  //                (b.name -> file))
  //
  //            case b: BodyPart =>
  //              // collect form field values
  //              b.toStrict(2.seconds).map(strict =>
  //                (b.name -> strict.entity.data.utf8String))
  //
  //          }.runFold(Map.empty[String, Any])((map, tuple) => map + tuple)
  //
  ////          val done = allPartsF.map { allParts =>
  ////            // You would have some better validation/unmarshalling here
  ////            doCall({
  ////              exec(allParts("file").asInstanceOf[File])
  ////            })
  ////            db.create(Video(
  ////              file = allParts("file").asInstanceOf[File],
  ////              title = allParts("title").asInstanceOf[String],
  ////              author = allParts("author").asInstanceOf[String]))
  ////          }
  //
  //          // when processing have finished create a response for the user
  //          onSuccess(allPartsF) { allParts =>
  //            doCall({
  //              exec(allParts("file").asInstanceOf[File])
  //            })
  //          }
  //        }
  //      }
  //    }
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

