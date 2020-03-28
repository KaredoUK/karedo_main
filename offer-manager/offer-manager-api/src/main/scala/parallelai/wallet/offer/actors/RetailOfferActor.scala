package parallelai.wallet.offer.actors

import akka.actor.Actor.Receive
import parallelai.wallet.cassandra.RetailOfferRecord
import akka.actor.Actor
import parallelai.wallet.entity.RetailOffer
import java.util.UUID
import org.joda.time.DateTime

object RetailOfferActor {
  object In {}
}

/**
 * Created by crajah on 27/04/2014.
 */
class RetailOfferActor extends Actor {
  import RetailOfferActor._

  def receive: Receive = {
    case In => RetailOfferRecord.getEntireTable
    case retailOffer@RetailOffer(id, title, description, price, imageUrl, props, timestamp, test) => RetailOfferRecord.insertNewRecord(retailOffer)
  }
}
