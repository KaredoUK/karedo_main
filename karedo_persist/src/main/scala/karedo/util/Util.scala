package karedo.util

import java.net.Socket

import org.joda.time.{DateTime, DateTimeZone}

import scala.util.{Failure, Success, Try}
import java.util.UUID
import java.security.MessageDigest
import java.security.MessageDigest

/**
  * Created by pakkio on 10/7/16.
  */
object Util extends Configurable {

  def hex(buf: Array[Byte]): String = buf.map("%02X" format _).mkString

  def now = new DateTime(DateTimeZone.UTC)
  def newUUID = UUID.randomUUID().toString()
  def newMD5 = hex(MessageDigest.getInstance("MD5").digest(newUUID.getBytes()))
  def newCode = newMD5

//  def serverListening(host: String, port: Int, reason: String) = {
//    Try {
//      new Socket(host, port)
//    } match {
//      case Success(s) => {
//        s.close
//        true
//      }
//      case Failure(err) => false
//    }
//  }
//
//  def isWebPortFree() =
//    if (serverListening(conf.getString("web.host"), conf.getInt("web.port"), "is free"))
//      throw new Exception("A server is already listening on that port")
//
//  def isMongoActive() =
//    if (!serverListening(conf.getString("mongo.server.host"), conf.getInt("mongo.server.port"), "is active"))
//      throw new Exception("No mongodb listening on that port")


}
