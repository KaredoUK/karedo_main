import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global
import HttpMethods._
import akka.util.ByteString

object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()


  val http = Http()



  val postData = HttpRequest(POST, uri = "http://rtb.karedo.co.uk:12339/bids",
    entity = HttpEntity(ContentTypes.`application/json`,"""{"id":"2508744128", "timestamp":"2016-08-10T21:27:29.450Z", "isTest":False, "url":"http://datacratic.com/", "language":"en", "exchange":"mock", "location":{"countryCode":"CA", "regionCode":"QC", "cityName":"Montreal", "dma":-1, "metro":-1, "timezoneOffsetMinutes":-1}, "userIds":{"prov":"1568485738", "xchg":"1475615520"}, "imp":[{"id":"1", "formats":["160x600"], "position":0}, {"id":"2", "formats":["160x600"], "position":0}], "spots":[{"id":"1", "formats":["160x600"], "position":0}, {"id":"2", "formats":["160x600"], "position":0}]}"""))
  val response = http.singleRequest(postData)




  response.onComplete( x => print(x))
  Thread.sleep(5000)
}