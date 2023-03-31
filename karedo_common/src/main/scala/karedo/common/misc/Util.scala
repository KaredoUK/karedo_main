package karedo.common.misc

import java.security.MessageDigest
import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}

/**
  * Created by pakkio on 10/7/16.
  */
trait Utils {
  def hex(buf: Array[Byte]): String = buf.map("%02X" format _).mkString

  def now = new DateTime(DateTimeZone.UTC)
  def newUUID = UUID.randomUUID().toString()
  def newMD5 = hex(MessageDigest.getInstance("MD5").digest(newUUID.getBytes()))
  def newCode = newMD5
}

object Util extends Utils

