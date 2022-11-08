package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.ads.Kar134_adsActor
import karedo.routes.KaredoRoute
import akka.http.scaladsl._
import karedo.rtb.model.AdModel.DeviceRequest
import karedo.util.KaredoConstants
import model.headers._

/**
  * Created by pakkio on 10/3/16.
  */
object Kar134_ads extends KaredoRoute
  with Kar134_adsActor {

  def route = {
    Route {
      // GET /account/{{account_id}}/ads?p={{application_id}}&s={{session_id}}&c={{ad_count}}
      path("account" / Segment / "ads") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              optionalHeaderValueByName("User-Agent") {
                ua =>
                  optionalHeaderValueByName("X-Forwarded-For") {
                    xff =>
                      extractClientIP {
                        ip =>
                          get {
                            parameters('p, 's ?, 'c.as[Int], 'lat.as[Double].?, 'lon.as[Double].?, 'ifa ?, 'make ?, 'model ?, 'os ?, 'osv ?, 'did ?, 'dpid ?, 'mac ?, 'cc ?) {
                              (applicationId, sessionId, adCount, lat, lon, ifa, make, model, os, osv, did, dpid, mac, cc) =>
                                doCall({
                                  System.setProperty("java.net.preferIPv4Stack" , "true")

                                  val devObj = DeviceRequest(
                                    ua = ua,
                                    xff = xff,
                                    ifa = ifa,
                                    deviceType = Some(getDeviceType(make, model)),
                                    ip = if( ip.getAddress.isPresent) Some(ip.getAddress.get.getHostAddress) else None,
                                    make = make,
                                    model = model,
                                    os = os,
                                    osv = osv,
                                    did = did,
                                    dpid = dpid,
                                    mac = mac,
                                    lat = lat,
                                    lon = lon,
                                    country = cc
                                  )
                                  exec(accountId, deviceId, applicationId, sessionId, adCount, devObj)
                                }
                                )
                            }
                          }
                      }
                  }

              }
          }
      }
    }
  }
}
