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

  val app_included = true
  val app_id = "karedo"
  val app_name = "Karedo"
  val app_bundle = "uk.co.karedo"
  val app_domain = "karedo.co.uk"
  val app_storeurl_ios = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=1195450203&mt=8"
  val app_storeurl_android = "tbc"
  val app_privacypolicy = 1
  val app_paid = 0
  val secure_ad = 0

  val site_included = false
  val site_id = "karedo"
  val site_name = "Karedo"
  val site_domain = "karedo.co.uk"
  val site_privacypolicy = 1
  val site_page = "http://karedo.co.uk/main"

  val floor_price = 0.50

  val rtb_max_wait = (bid_tmax * 20) milliseconds
  val dispatcher_max_wait = (bid_tmax * 100) milliseconds
}
