package api


import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse}

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Await._
import scala.concurrent.Future
import scala.concurrent.duration._

class BrandServiceSpec extends Specification with NoTimeConversions {


  def wait[T](future: Future[T]): T = result(future, 20.seconds)

  implicit val system = ActorSystem()

  import system.dispatcher

  // execution context for futures
  val pipeline = {
    sendReceive ~> unmarshal[BrandResponse]
  }
  val url = "http://localhost:8080/"


  "Brand Service" >>  {
    "can create a new brand" in {

      val response = wait(pipeline {
        Post(url+"brand", BrandData("brand X", "iconpath"))
      })

      println("Returned UUID: "+response.id)

      true

    }
  }

}
