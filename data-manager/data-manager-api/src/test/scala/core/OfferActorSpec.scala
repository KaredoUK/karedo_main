package core

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import com.parallelai.wallet.datamanager.data.{GetOfferCodeResponse, GetOfferCodeRequest, InteractionResponse, UserBrandInteraction}
import org.specs2.matcher.BeMatching
import parallelai.wallet.entity.{AdvertisementDetail, KaredoSales}
import util.ActorsSpec

class OfferActorSpec
  extends ActorsSpec
  with TestKitBase
  with ImplicitSender
{
  isolated
  implicit lazy val system = ActorSystem("AkkaCustomSpec")
  val aUser: UUID = UUID.randomUUID()
  val aBrand: UUID = UUID.randomUUID()
  val anOffer: UUID = UUID.randomUUID()

  "Offer Actor" >> {

    "should call the actor" in new WithMockedPersistence {
      mockedSaleDAO.findById(any[UUID]) returns Some(KaredoSales(saleType="OFFER",points=10,accountId = aUser, adId = Some(anOffer),
        code=Some("")))
      mockedSaleDAO.findByCode(any[String]) returns None
      mockedSaleDAO.insertNew(any[KaredoSales]) returns Some(UUID.randomUUID())
      mockedBrandDAO.getAdById(any[UUID]) returns Some(AdvertisementDetail(anOffer))

      server.offer ! GetOfferCodeRequest(aUser, aBrand)
      expectMsgPF() {
        case SuccessResponse(obj) =>
        {
          val resp=obj.asInstanceOf[GetOfferCodeResponse]
          println(s"code received is ${resp.code}")
          resp.code must beMatching("[0-9A-Z]{8}".r)

        }
      }
    }
  }
}
