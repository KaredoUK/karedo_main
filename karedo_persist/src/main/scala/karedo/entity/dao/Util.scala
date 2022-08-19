package karedo.entity.dao

import org.joda.time.{DateTime, DateTimeZone}

/**
  * Created by pakkio on 10/7/16.
  */
object Util {

  def now = new DateTime(DateTimeZone.UTC)

}
