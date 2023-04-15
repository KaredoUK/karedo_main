package karedo.rtb.runner

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import karedo.persist.entity.KaredoChange
import karedo.rtb.actor.AdActor
import karedo.rtb.model.AdModel._
import karedo.common.misc.Util.newUUID
import karedo.rtb.util.{DeviceMakes, RtbConstants}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import karedo.rtb.model.AdModel._

/**
  * Created by crajah on 03/12/2016.
  */
object BidRunner extends App with DeviceMakes with RtbConstants {

  val count = 2

  try {
    implicit val duration: Timeout = 5 second

    val user_id = "9c025f65-4949-4e29-9ced-179cedbf0e2e"
    val lat:Double = 0.0
    val lon:Double = 0.0

    println("Calling AdActor")

    val adActor = new AdActor

    val device = DeviceRequest(lat = Some(lat), lon = Some(lon))

    val adBack = adActor.getAds(AdRequest(user_id, count, device))

    println("Call Complete " + adBack)

    adBack.foreach {a => println(a.toJson.toString)}

  } catch {
    case e: Exception => println(e)
  }

}

