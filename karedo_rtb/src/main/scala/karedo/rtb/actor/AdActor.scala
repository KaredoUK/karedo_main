package karedo.rtb.actor



import java.io.File

import akka.actor.Actor
import karedo.rtb.model._
import karedo.rtb.model.BidModel._
import karedo.rtb.model.AdModel._
import karedo.util.{KO, OK, Result}
import karedo.rtb.model.DbCollections

/**
  * Created by crajah on 25/08/2016.
  */
class AdActor extends Actor with DbCollections {
  def receive = {
    case AdRequest(userId, count) => {
      // @TODO: get UserProfile
      val userProfileResult = dbUserProfile.find(userId.toString)

      if( userProfileResult.isOK ) {

      }

      // @TODO: Fill BidRequest
      // @TODO: Send it to auctioneers

      // @TODO: Return AdResponse

      // Fake it till you make it.
      val adUnits = scala.collection.mutable.ListBuffer.empty[AdUnit]


      (1 to count).foreach(x => {
        val u = makeAdUrlPair
        adUnits += AdUnit(
            ad_type_IMAGE,
            java.util.UUID.randomUUID.toString,
            java.util.UUID.randomUUID.toString,
            Ad( u._1, u._2, u._3, None, Some(250), Some(300), None),
            Math.random() * 100,
            Some(List("karedo.co.uk")),
            None,
            None,
            Some(java.util.UUID.randomUUID.toString),
            Some(java.util.UUID.randomUUID.toString),
            300,
            250
          )
      })

      sender ! OK(AdResponse(adUnits.length, adUnits.toList))
    }
  }

  def makeAdUrlPair: (String, String, String) = {

    val ads = Array(
      ("http://karedo.co.uk/ads/c1.jpg",
        "https://www.washingtonpost.com/news/answer-sheet/wp/2015/03/26/no-finlands-schools-arent-giving-up-traditional-subjects-heres-what-the-reforms-will-really-do/",
        "No, Finland isn’t ditching traditional school subjects. Here’s what’s really happening."),
      ("http://karedo.co.uk/ads/c2.jpg", "http://www.iflscience.com/health-and-medicine/paralyzed-monkeys-walk-again-thanks-brain-implant/",
        "Paralyzed Monkeys Walk Again Thanks To Brain Implant"),
      ("http://karedo.co.uk/ads/c3.jpg",
        "http://www.financeworld.news/index.php/2016/11/02/rich-bankers-terrified/",
        "Rich Bankers Are Terrified Their Secret Weapon Is Finally Revealed"),
      ("http://karedo.co.uk/ads/c4.jpg", "http://www.nytimes.com/2016/11/12/business/economy/donald-trump-trade-tpp-trans-pacific-partnership.html",
        "What Is Lost by Burying the Trans-Pacific Partnership?"),
      ("http://karedo.co.uk/ads/c5.jpg", "http://www.bbc.co.uk/news/uk-37951771", "Armistice Day in pictures"),
      ("http://karedo.co.uk/ads/c6.jpg", "http://www.forbes.com/sites/kathleenchaykowski/2016/11/10/mark-zuckerberg-says-fake-news-on-facebook-did-not-sway-the-u-s-election/#4415191146a5",
        "Mark Zuckerberg Says Fake News On Facebook Did Not Sway The U.S. Election"),
      ("http://karedo.co.uk/ads/c7.png", "https://www.theguardian.com/sport/blog/2016/nov/10/conor-mcgregor-eddie-alvarez-ufc-205",
        "The cheapening of Conor McGregor's once-clever act"),
      ("http://karedo.co.uk/ads/c8.jpg", "http://www.nytimes.com/2016/11/12/technology/10-second-videos-from-your-sunglasses-thank-snapchat.html?ref=technology",
        "10-Second Videos From Your Sunglasses. Thank Snapchat.")
    )

    val i:Int = (Math.random() * ads.length).toInt

    ads(i)
  }
}

class Auctioneer extends Actor {
  def receive = {
    case BidRequest_2_3_1(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_) => {
      // @TODO: make requests to the AdExcahnge,
      // @TODO: timeout after TTL
      // @TODO: return the BidResponse
      // @TODO: Update the win notice

    }
  }
}
