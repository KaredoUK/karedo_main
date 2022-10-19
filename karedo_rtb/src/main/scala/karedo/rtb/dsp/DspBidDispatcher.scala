package karedo.rtb.dsp

import karedo.rtb.model.AdModel.AdUnit
import karedo.entity._
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.util.DeviceMake

import scala.concurrent.Future

/**
  * Created by crajah on 28/11/2016.
  */
trait DspBidDispather  {
  def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake): List[AdUnit]
}

case class DspBidDispatcherConfig(name: String, kind: DspKind, scheme: HttpScheme, host: String, port: Int = 80, path: String, endpoint: String )

sealed trait HttpScheme
case object HTTP extends HttpScheme
case object HTTPS extends HttpScheme

sealed trait DspKind
case object DUMMY extends DspKind
case object ORTB2_2 extends DspKind