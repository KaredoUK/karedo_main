package karedo.rtb

import com.typesafe.config.Config

/**
  * Created by charaj on 28/03/2017.
  */
package object dsp {
  case class DspBidDispatcherConfig
  (name: String,
   kind: DspKind,
   scheme: HttpScheme,
   markup: MarkupScheme,
   host: String,
   port: Int = 80,
   path: String,
   endpoint: String,
   price_cpm: Double,
   comm_percent: Double,
   config: Config )

  sealed trait HttpScheme
  case object HTTP extends HttpScheme
  case object HTTPS extends HttpScheme

  sealed trait DspKind
  case object DUMMY extends DspKind
  case object ORTB2_2 extends DspKind
  case object SMAATO extends DspKind
  case object MOBFOX extends DspKind
  case object FEED extends DspKind
  case object STORED extends DspKind


  sealed trait MarkupScheme
  case object NURL extends MarkupScheme
  case object ADM extends MarkupScheme
  case object RESP extends MarkupScheme
}
