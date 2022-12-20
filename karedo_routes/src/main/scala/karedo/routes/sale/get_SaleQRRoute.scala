package karedo.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import java.io.File

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.Multipart.ByteRanges.BodyPart
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.scaladsl.{FileIO, Sink}
import karedo.actors.sale._
import karedo.routes.KaredoRoute
import akka.http.scaladsl.model.{HttpEntity, _}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pakkio on 10/3/16.
  */
object get_SaleQRRoute extends KaredoRoute
  with get_SaleQRActor {

  def route = {
    Route {

      path("sale" / Segment / "qr" ) {
        saleId =>
              get {
                    doCall({
                      exec(saleId)
                    }
                    )
                }
              }
    }
  }
}

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
