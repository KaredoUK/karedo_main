package org.joda.time
/**
  * Created by charaj on 20/01/2017.
  */
object JodaAccessor {
  def localMillis(localDateTime: LocalDateTime) =
    localDateTime.getLocalMillis
  def localMillis(localDate: LocalDate) =
    localDate.getLocalMillis
}