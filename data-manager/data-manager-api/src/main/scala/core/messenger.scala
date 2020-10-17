package core

import java.net.URI

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

object MessengerActor {

  case class SendMessage(to: URI, message: String, subject: String = "")

  def props(emailActor: ActorRef, smsActor: ActorRef)(implicit bindingModule: BindingModule) = Props(new MessengerActor(emailActor, smsActor) )

}

class MessengerActor(email: ActorRef, sms: ActorRef)(implicit val bindingModule: BindingModule ) extends Actor with Injectable with ActorLogging {
  import MessengerActor._
  import EmailActor._
  import SMSActor._
  import parallelai.wallet.config.ConfigConversions._

  val smsCopyForwardEmailAddressesOp = injectOptionalProperty[List[String]]("notification.sms.forward.email.list") getOrElse List.empty[String]

  def receive: Receive = {
    case SendMessage(to, message, subject) if to.getScheme == "mailto" =>
      email ! SendEmail(to.getRawSchemeSpecificPart, message, subject)

    case SendMessage(to, message, _) if to.getScheme ==  "sms" =>
      val targetMsisdn = to.getRawSchemeSpecificPart
      sms ! SendSMS(targetMsisdn, message)

      val emailSubject = s"SMS to: $targetMsisdn\n"
      smsCopyForwardEmailAddressesOp foreach { recipient =>
        log.info(s"Sending copy of SMS to recipient $recipient")

        email ! SendEmail(recipient, message, emailSubject)
      }
  }
}
