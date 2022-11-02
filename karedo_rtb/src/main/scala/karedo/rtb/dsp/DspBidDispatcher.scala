package karedo.rtb.dsp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import karedo.rtb.model.AdModel.{AdUnit, DeviceRequest}
import karedo.entity._
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.util.DeviceMake
import com.typesafe.config.Config

import scala.concurrent.Future

/**
  * Created by crajah on 28/11/2016.
  */
trait DspBidDispather  {
  def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): List[AdUnit]
}

case class DspBidDispatcherConfig
(name: String,
 kind: DspKind,
 scheme: HttpScheme,
 markup: MarkupScheme,
 host: String,
 port: Int = 80,
 path: String,
 endpoint: String,
 config: Config )

sealed trait HttpScheme
case object HTTP extends HttpScheme
case object HTTPS extends HttpScheme

sealed trait DspKind
case object DUMMY extends DspKind
case object ORTB2_2 extends DspKind
case object SMAATO extends DspKind

object HttpDispatcher {
  implicit val actor_system = ActorSystem("rtb")
  implicit val actor_materializer = ActorMaterializer()

  val httpDispatcher = Http()
  val httpsDispatcher = Http()
}

sealed trait MarkupScheme
case object NURL extends MarkupScheme
case object ADM extends MarkupScheme
case object RESP extends MarkupScheme