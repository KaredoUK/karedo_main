package core

import java.net.URI

import akka.actor.{ActorRef, Props, Actor}
import java.util.UUID

object MessengerActor {

  case class SendMessage(to: URI, message: String, subject: String = "")

  def props(emailActor: ActorRef, smsActor: ActorRef) = Props(classOf[MessengerActor], emailActor, smsActor)

}

class MessengerActor(email: ActorRef, sms: ActorRef) extends Actor {
  import MessengerActor._
  import EmailActor._
  import SMSActor._

  def receive: Receive = {
    case SendMessage(to, message, subject) if to.getScheme == "mailto" =>
      email ! SendEmail(to.getRawSchemeSpecificPart, message, subject)

    case SendMessage(to, message, _) if to.getScheme ==  "sms" =>
      sms ! SendSMS(to.getRawSchemeSpecificPart, message)
  }
}
