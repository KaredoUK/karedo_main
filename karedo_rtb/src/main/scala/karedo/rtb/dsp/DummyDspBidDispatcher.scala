package karedo.rtb.dsp

import java.util.concurrent.Executors

import karedo.entity.UserPrefData
import karedo.rtb.model.AdModel.{Ad, _}
import karedo.rtb.model.BidJsonImplicits

import scala.concurrent.{ExecutionContext, Future}
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.util.{DeviceMake, LoggingSupport}

/**
  * Created by crajah on 28/11/2016.
  */

class DummyDspBidDispatcher(config: DspBidDispatcherConfig)
  extends DspBidDispather(config)
    with LoggingSupport
    with BidJsonImplicits {

  override def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): List[AdUnit] = {
    logger.debug(marker, s"IN: DummyDspBidDispatcher.getAds. Count is ${count}, User is: ${user}, Device is ${device}" )

      val adUnits = scala.collection.mutable.ListBuffer.empty[AdUnit]

      val salt = (Math.random() * 100).toInt

      (1 to count).foreach(x => {
        val u = makeAdUrlPair(x + salt)
        adUnits += AdUnit(
          ad_type_IMAGE,
          java.util.UUID.randomUUID.toString,
          java.util.UUID.randomUUID.toString,
          Ad( u._1, u._2, u._3, u._4, None, Some(250), Some(300), None),
          0.25,
          Some(List("karedo.co.uk")),
          None,
          None,
          java.util.UUID.randomUUID.toString,
          java.util.UUID.randomUUID.toString,
          300,
          250
        )
      })

      adUnits.toList
  }

  def makeAdUrlPair(index: Int): (String, String, String, Option[String]) = {

    val ads = Array(
      ("http://karedo.co.uk/ads/c1.jpg",
        "https://www.washingtonpost.com/news/answer-sheet/wp/2015/03/26/no-finlands-schools-arent-giving-up-traditional-subjects-heres-what-the-reforms-will-really-do/",
        "No, Finland isn’t ditching traditional school subjects. Here’s what’s really happening.", Some("Washington Post")),
      ("http://karedo.co.uk/ads/c2.jpg", "http://www.iflscience.com/health-and-medicine/paralyzed-monkeys-walk-again-thanks-brain-implant/",
        "Paralyzed Monkeys Walk Again Thanks To Brain Implant", None),
      ("http://karedo.co.uk/ads/c3.jpg",
        "http://www.financeworld.news/index.php/2016/11/02/rich-bankers-terrified/",
        "Rich Bankers Are Terrified Their Secret Weapon Is Finally Revealed", Some("Finance World")),
      ("http://karedo.co.uk/ads/c4.jpg", "http://www.nytimes.com/2016/11/12/business/economy/donald-trump-trade-tpp-trans-pacific-partnership.html",
        "What Is Lost by Burying the Trans-Pacific Partnership?", Some("New York Times")),
      ("http://karedo.co.uk/ads/c5.jpg", "http://www.bbc.co.uk/news/uk-37951771", "Armistice Day in pictures", Some("BBC")),
      ("http://karedo.co.uk/ads/c6.jpg", "http://www.forbes.com/sites/kathleenchaykowski/2016/11/10/mark-zuckerberg-says-fake-news-on-facebook-did-not-sway-the-u-s-election/#4415191146a5",
        "Mark Zuckerberg Says Fake News On Facebook Did Not Sway The U.S. Election", None),
      ("http://karedo.co.uk/ads/c7.jpg", "https://www.theguardian.com/sport/blog/2016/nov/10/conor-mcgregor-eddie-alvarez-ufc-205",
        "The cheapening of Conor McGregor's once-clever act", Some("Guardian")),
      ("http://karedo.co.uk/ads/c8.jpg", "http://www.nytimes.com/2016/11/12/technology/10-second-videos-from-your-sunglasses-thank-snapchat.html?ref=technology",
        "10-Second Videos From Your Sunglasses. Thank Snapchat.", Some("New York Times"))
    )

    val i:Int = (index % ads.length)

    ads(i)
  }

}
