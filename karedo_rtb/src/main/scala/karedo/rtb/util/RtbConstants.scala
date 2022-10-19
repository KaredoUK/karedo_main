package karedo.rtb.util

import scala.concurrent.duration._

/**
  * Created by crajah on 04/12/2016.
  */
trait RtbConstants {
  val bid_tmax = 250
  val bid_bcat = List("IAB25", "IAB26")

  val banner_w = 300
  val banner_h = 250
  val banner_pos = 1
  val banner_btype = List(1, 3, 4)

  val app_id = "karedo"
  val app_name = "Karedo"
  val app_bundle = "uk.co.karedo"
  val app_domain = "karedo.co.uk"
  val app_storeurl_ios = "tbc"
  val app_storeurl_android = "tbc"
  val app_privacypolicy = 1
  val app_paid = 0

  val rtb_max_wait = (bid_tmax * 20) milliseconds
  val dispatcher_max_wait = (bid_tmax * 100) milliseconds
}
