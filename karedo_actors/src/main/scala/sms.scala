import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import karedo.entity.dao.Configurable


object SMSActor {
  case class SendSMS(to: String, message: String, retryCount: Int = 3)

  def props = Props(new SMSActor)

  def normaliseMsisdn(msisdn: String): String = {
    val internationalWithPlus = """\+(\d+)""".r
    val internationalWithDoubleZero = """00(\d+)""".r
    val genericNumeric = """(\d+)""".r

    msisdn match {
      case internationalWithPlus(number)       => number
      case internationalWithDoubleZero(number) => number
      case genericNumeric(number)              => number
      case _                                   => throw new IllegalArgumentException(s"Invalid msisdn $msisdn")
    }
  }
}

import SMSActor._




class SMSActor
  extends Configurable
    with Actor
    with SMSTrait
    with ActorLogging {
  override val accessKey = conf.getString("notification.sms.auth.accesskey")
  override val serverEndpoint = conf.getString("notification.sms.server.endpoint")
  override val from = conf.getString("notification.sms.sender")
  // needed for compatibility
  override val system = context.system
  override val materializer = ActorMaterializer()


  log.info(s"[SMS] [T:${Thread.currentThread().getId}]  instantiating SMS with '$serverEndpoint' and accessKey '${accessKey}'")

  import context.dispatcher



  def receive: Receive = {
    case request @ SendSMS(number, body, retryCount) => sendSMS(number, body) recover {
      case exception if (retryCount > 0) =>
        log.debug("Failed to send message {} because of exception, retrying", request, exception)
        self ! SendSMS(number, body, retryCount - 1)
      case exception =>
        log.error("Unable to send message {}, exception {}", request, exception)
    }
  }

}
