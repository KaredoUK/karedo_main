package karedo.rtb.dsp

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.Uri.{Path, Query}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, Uri, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import karedo.entity.AdUnitType
import karedo.rtb.model.AdModel.{Ad, AdUnit}

import scala.concurrent.Await

/**
  * Created by charaj on 12/02/2017.
  */
object AdMechanic extends HttpDispatcher {
  def adUntiTypeToAdUnit(adUnitTypes: List[AdUnitType]): List[AdUnit] = {
    adUnitTypes.map {
      adu =>
        AdUnit(
          ad_type = adu.ad_type,
          ad_id = adu.id,
          impid = adu.impid,
          ad = Ad(
            imp_url = Some(adu.ad.imp_url),
            click_url = adu.ad.click_url,
            ad_text = adu.ad.ad_text,
            ad_source = adu.ad.ad_source,
            duration = adu.ad.duration,
            h = adu.ad.h,
            w = adu.ad.w,
            beacons = None
          ),
          price_USD_per_1k = adu.price_USD_per_1k, // In USD eCPM
          ad_domain = adu.ad_domain,
          iurl = adu.iurl,
          nurl = adu.nurl,
          cid = adu.cid,
          crid = adu.crid,
          w = adu.w,
          h = adu.h,
          hint = adu.hint

        )
    }
  }
}
