package karedo.common.crypto

import org.scalatest._

/**
  * Created by charaj on 17/05/2017.
  */
class CryptoTest extends WordSpec with Matchers with WithCrypto {
  "Cryptographic Layer" should {
    "* testing DES" in {
      val sha_key = toSHA256("hello".getBytes("utf-8"))
      setSecret(sha_key)
      setAlgorithm(DES)
      setCharset("utf-8")
      val msg = "This is a new logic"
      val code = encrypt(msg)
      val new_msg:String = decrypt(code)
      msg shouldEqual new_msg
    }

    "* testing AES" in {
      val sha_key = toSHA256("hello".getBytes("utf-8"))
      val reduced_key = getFirstNBytes(sha_key, 16)
      setSecret(reduced_key)
      setAlgorithm(AES)
      setCharset("utf-8")

      val msg = "This is a new logic"
      val code = encrypt(msg)
      val new_msg:String = decrypt(code)
      msg shouldEqual new_msg
    }

    "* Default Crypto" in {
      val crypto = DefaultDESCrypto

      val secret = crypto.getDefaultSecret
      val keyStore = crypto.getDefaultKeyStore

      val msg = "This is a new logic"
      val code = crypto.encrypt(msg)
      val new_msg:String = crypto.decrypt(code)
      msg shouldEqual new_msg
    }
  }
}
