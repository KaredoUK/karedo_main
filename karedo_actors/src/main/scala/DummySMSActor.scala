import SMSActor.SendSMS
import akka.actor.{Actor, ActorLogging, Props}
import karedo.entity.dao.Configurable

// used for tests
class DummySMSActor extends Configurable
  with Actor
  with ActorLogging {

  log.info(s"[DUMMYSMS] [T:${Thread.currentThread().getId}]  instantiating DUMMY SMSActor")
  def receive: Receive = {
    case request @ SendSMS(number, body, retryCount) =>
      log.info(s"[DUMMYSMS] (sms to: $number, body: $body)")
  }
}
object DummySMSActor {
  def props = Props(new DummySMSActor)
}