import java.util.UUID

import SMSActor.SendSMS
import akka.actor.{Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{Matchers, WordSpecLike}


class SmsSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers {

  "sms actor" should {
    "work well" in {

      val actorRef = TestActorRef(new SMSActor)
      val actor = actorRef.underlyingActor
      val number: String = ""// 00393319345235"
      val body: String = "hello world " + UUID.randomUUID().toString
      val ret = actorRef ! SendSMS(number, body, 1)
      ret should be(Unit)
    }

  }

}
