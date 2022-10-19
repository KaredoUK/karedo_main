package karedo.rtb.util

/**
  * Created by crajah on 05/12/2016.
  */
object RTBUtils {
  def getSHA1Hash(s: String): String = {
    java.security.MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }

  def getMD5Hash(s: String): String = {
    java.security.MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }

}
