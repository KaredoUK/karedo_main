package karedo.api.account

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import karedo.api.account.messages.RegisterRequest
import play.api.libs.json.{Format, Json}

/**
  * The SoT service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the SotService.
  */
trait AccountService extends Service {

  def register(): ServiceCall[RegisterRequest, Done]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("account")
      .withCalls(
        restCall(Method.POST, "/v2/account/register", register _ )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}


