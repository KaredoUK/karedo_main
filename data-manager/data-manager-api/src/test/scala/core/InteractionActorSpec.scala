package core

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import com.parallelai.wallet.datamanager.data.{InteractionResponse, UserBrandInteraction}
import parallelai.wallet.entity.KaredoTypes.KaredoPoints
import parallelai.wallet.entity.{UserAccountTotalPoints, UserAccount, Brand}
import util.ActorsSpec

class InteractionActorSpec
  extends ActorsSpec
  with TestKitBase
  with ImplicitSender
{
  isolated
  implicit lazy val system = ActorSystem("AkkaCustomSpec")
  val aUser: UUID = UUID.randomUUID()
  val aBrand: UUID = UUID.randomUUID()


  "Interaction Actor" >> {

    "should answer 10 points for sharing a brand" in new WithMockedPersistence {

      mockedBrandDAO.getById(aBrand) returns Some(Brand(id=aBrand))
      mockedUserAccountDAO.getById(aUser) returns Some(UserAccount(id=aUser,msisdn=Some("1"),email=Some("")))
      mockedUserAccountDAO.addPoints(any[UUID], any[KaredoPoints]) returns Some(UserAccountTotalPoints(10))

      server.brand ! UserBrandInteraction(aUser, aBrand, "share")
      expectMsg(SuccessResponse(InteractionResponse(aUser, 10)))
    }
    "should answer 1 points for liking a brand" in new WithMockedPersistence {

      mockedBrandDAO.getById(aBrand) returns Some(Brand(id=aBrand))
      mockedUserAccountDAO.getById(aUser) returns Some(UserAccount(id=aUser,msisdn=Some("1"),email=Some("")))
      mockedUserAccountDAO.addPoints(any[UUID], any[KaredoPoints]) returns Some(UserAccountTotalPoints(5))

      server.brand ! UserBrandInteraction(aUser, aBrand, "like")
      expectMsg(SuccessResponse(InteractionResponse(aUser, 5)))
    }
  }
}
