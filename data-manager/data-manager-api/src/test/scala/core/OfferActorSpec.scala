package core

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import com.parallelai.wallet.datamanager.data.{GetOfferCodeResponse, GetOfferCode, InteractionResponse, UserBrandInteraction}
import org.specs2.matcher.BeMatching
import parallelai.wallet.entity.Sale
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

  "Offer Actor" >> {

    "should call the actor" in new WithMockedPersistence {
      mockedSaleDAO.getById(any[UUID]) returns Some(Sale(userId = aUser, adId = aBrand, code=""))
      mockedSaleDAO.getByCode(any[String]) returns None
      mockedSaleDAO.insertNew(any[Sale]) returns Some(UUID.randomUUID())

      server.offer ! GetOfferCode(aUser, aBrand)
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
