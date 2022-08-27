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
            Ad( u._1, u._2, None, Some(250), Some(300), None),
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

  def makeAdUrlPair: (String, String) = {

    val ads = Array(
      ("http://karedo.co.uk/ads/ad_300x250_1.jpg", "http://www.bbc.co.uk"),
      ("http://karedo.co.uk/ads/ad_300x250_2.gif", "http://www.google.co.uk"),
      ("http://karedo.co.uk/ads/ad_300x250_3.jpg", "http://www.yahoo.com"),
      ("http://karedo.co.uk/ads/ad_300x250_4.jpg", "http://www.nytimes.com"),
      ("http://karedo.co.uk/ads/ad_300x250_5.jpg", "http://www.audi.com"),
      ("http://karedo.co.uk/ads/ad_300x250_6.jpg", "http://www.karedo.co.uk"),
      ("http://karedo.co.uk/ads/ad_300x250_7.png", "http://www.guardian.com"),
      ("http://karedo.co.uk/ads/ad_300x250_8.jpg", "http://www.times.com")
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
