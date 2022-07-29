package karedo.rtb.actor


import akka.actor.Actor
import karedo.rtb.model._

/**
  * Created by crajah on 25/08/2016.
  */
class AdBidActor extends Actor {
  def receive = {
    case AdRequest(userId, count) => {

    }
  }
}

class Auctioneer extends Actor {
  def receive = {
    case BidRequest(_,_,_,_,_,_,_,_,_,_,_,_) => {
      // make requests to the AdExcahnge,
      // timeout after TTL
      // return the BidResponse
      // Update the win notice
    }
  }
}
