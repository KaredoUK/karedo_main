package karedo.api.account.events

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}

import karedo.api.account.model.UserApp
import play.api.libs.json.{Format, Json}

sealed trait AccountEvent extends AggregateEvent[AccountEvent] {
  def aggregateTag = AccountEvent.Tag
}

object AccountEvent {
  val Tag = AggregateEventTag[AccountEvent]
}

case class UserAppChangedEvent(userApp: UserApp) extends AccountEvent
object UserAppChangedEvent {
  implicit val format: Format[UserAppChangedEvent] = Json.format[UserAppChangedEvent]
}
