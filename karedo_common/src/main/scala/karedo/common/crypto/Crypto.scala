package karedo.common.crypto

import java.security.{Key, MessageDigest}
import java.util.Base64
import javax.crypto._
import javax.crypto.spec.{DESKeySpec, PBEKeySpec, PBEParameterSpec, SecretKeySpec}

import com.sun.crypto.provider.AESKeyGenerator

sealed trait Algorithm {
  def name: String
  def value: String
  override def toString = name
}

case object HS256 extends Algorithm {
  def name = "HS256"
  def value = "HmacSHA256"
}
case object HS384 extends Algorithm {
  def name = "HS384"
  def value = "HmacSHA384"
}
case object HS512 extends Algorithm {
  def name = "HS512"
  def value = "HmacSHA512"
}
case object NONE extends Algorithm {
  def name = "NONE"
  def value = "NONE"
}
case object AES extends Algorithm {
  def name = "AES"
  def value = "AES"
}
case object DES extends Algorithm {
  def name = "DES"
  def value = "DES"
}


object Algorithm {
  def apply(name: String): Algorithm = {
    name match {
      case s if s == HS256.name => HS256
      case s if s == HS384.name => HS384
      case s if s == HS512.name => HS512
      case s if s == AES.name => AES
      case s if s == DES.name => DES
      case s if s == NONE.name => NONE
      case _ => throw new Exception("Unknown Algorithm")
    }
  }
}

trait WithCrypto {
  private var algorithm: Algorithm = null
  private var secret: Array[Byte] = null
  private var charset: String = "utf-8"

  def setAlgorithm(alg: Algorithm) = algorithm = alg
  def setSecret(sec: Array[Byte]) = secret = sec
  def setCharset(set: String) = charset = set

  def getAlgorithm = algorithm
  def getCharset = charset

  def getSignature(payload: String): Array[Byte] = {
    require(algorithm != null, "Algorithm not set")
    require(secret != null, "Secret not set")

    algorithm match {
      case HS256 | HS384 | HS512 => {
        val mac: Mac = Mac.getInstance(algorithm.value)
        mac.init(new SecretKeySpec(secret, algorithm.value))
        mac.doFinal(payload.getBytes(charset))
      }
      case AES | DES => encrypt(payload)
      case NONE => "".getBytes(charset)
    }
  }

  def getB64Signature(payload: String): String = {
    Base64.getUrlEncoder.encodeToString(getSignature(payload))
  }

  private def getSecretKey(): Key = {
    require(secret != null)
    require(algorithm != null)

    algorithm match {
      case AES => (new SecretKeySpec(secret, algorithm.value)).asInstanceOf[Key]
      case DES => {
        val keySpec = new DESKeySpec(secret)
        SecretKeyFactory.getInstance(algorithm.value).generateSecret(keySpec)
      }
      case _ => new Key {override def getEncoded: Array[Byte] = secret

        override def getAlgorithm: String = algorithm.name

        override def getFormat: String = algorithm.value
      }
    }
  }

  def toB64[I,O](bytes: I)(implicit i:I => Array[Byte], o:Array[Byte] => O): O = Base64.getEncoder.encode(bytes)
  def fromB64[I,O](b64: I)(implicit i:I => Array[Byte], o:Array[Byte] => O): O = Base64.getDecoder.decode(b64)

  def toB64String[I](bytes: I)(implicit i:I => Array[Byte]):String = Base64.getEncoder.encodeToString(bytes)
  def fromB64String[O](b64: String)(implicit o:Array[Byte] =>O): O = Base64.getDecoder.decode(b64)

  def toSHA256[I,O](s:I)(implicit i:I => Array[Byte], o:Array[Byte] => O): O = MessageDigest.getInstance("SHA-256").digest(s)
  def toSHA1[I,O](s:I)(implicit i:I => Array[Byte], o:Array[Byte] => O): O = MessageDigest.getInstance("SHA-1").digest(s)

  def getFirstNBytes(a:Array[Byte], n: Int = 16): Array[Byte] = {
    val o = new Array[Byte](n)
    Array.copy(a, 0, o, 0, n)
    o
  }

  sealed trait CIPHER {
    def mode:Int
  }
  case object ENCRYPT extends CIPHER {
    def mode:Int = Cipher.ENCRYPT_MODE
  }
  case object DECRYPT extends CIPHER {
    def mode:Int = Cipher.DECRYPT_MODE
  }

  implicit def stringToBytes(s:String): Array[Byte] = s.getBytes(charset)
  implicit def bytesToString(b:Array[Byte]): String = new String(b)

  private def perform[I, O](msg: I, dir: CIPHER)(implicit i:I => Array[Byte], o:Array[Byte] => O): O = {
    require(algorithm match {
      case AES | DES => true
      case _ => false
    }, s"Cannot ${dir} with Algorithm ${algorithm}")

    val cipher = Cipher.getInstance(algorithm.value)
    cipher.init(dir.mode, getSecretKey())
    cipher.doFinal(msg)
  }

  def encrypt[I, O](msg: I)(implicit m:I => Array[Byte], n:Array[Byte] => O): O = perform(msg, ENCRYPT)
  def decrypt[I, O](code: I)(implicit m:I => Array[Byte], n:Array[Byte] => O) : O = perform(code, DECRYPT)
}

object DefaultDESCrypto extends WithCrypto with KeySupport {
  // Set the Algorithm to AES
  setAlgorithm(DES)
  setSecret(getDefaultSecret)
  setCharset("utf-8")
}