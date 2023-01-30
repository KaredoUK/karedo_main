package karedo.util


import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import spray.json._
import DefaultJsonProtocol._
import karedo.Api.{keyStoreName, keyStorePass, keyStoreType}
import karedo.util.DateTimeJsonHelper._

import scala.util.Try
import karedo.util.Util.now


/**
  * Created by charaj on 07/03/2017.
  */
trait JWTSupport extends DefaultJsonProtocol
with SprayJsonSupport {

  private var algorithm: Algorithm = null
  private var secret: String = null
  private var charset: String = "utf-8"

  def setAlgorithm(alg: Algorithm) = algorithm = alg
  def setSecret(sec: String) = secret = sec
  def setCharset(set: String) = charset = set

  case class JWTHeader
  (
    alg: String = "HS256",
    typ: String = "JWT"
  )
  implicit val json_JWTHeader = jsonFormat2(JWTHeader)

  case class JWTReservedClaims
  (
     exp: Option[DateTime] = None        // Expiry
    , nbf: Option[DateTime] = None        // Not Before
    , iat: Option[DateTime] = None        // Issued At
    , iss: Option[String] = None          // Issuer
    , aud: Option[String] = None          // Audience
    , prn: Option[String] = None          // Prinicpal
    , jti: Option[String] = None          // JWT ID
    , typ: Option[String] = None           // Type of Contents
  )
  implicit val json_JWTReservedClaims = jsonFormat8(JWTReservedClaims)

  case class JWTDefaultClaims(
    application_id: Option[String] = None
    , account_id: Option[String] = None
    , session_id: Option[String] = None
  )
  implicit val json_JWTPayload = jsonFormat3(JWTDefaultClaims)

  case class JWT
  (
    header: JWTHeader
    , reserved: JWTReservedClaims
    , default: JWTDefaultClaims
    , payload: Map[String, String]
  )
  implicit val json_JWT = jsonFormat4(JWT)


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

  object Algorithm {
    def apply(name: String): Algorithm = {
      name match {
        case s if s == HS256.name => HS256
        case s if s == HS384.name => HS384
        case s if s == HS512.name => HS512
        case s if s == NONE.name => NONE
        case _ => throw new Exception("Unknown Algorithm")
      }
    }
  }

  def getDefaultJWTToken(application_id: String, account_id: String, session_id: String) = {
    getJWTToken(getDefaultJWT(application_id, account_id, session_id))
  }

  def getDefaultJWT(application_id: String, account_id: String, session_id: String) = {
    JWT(
      JWTHeader(),
      JWTReservedClaims(exp = Some(now.plusDays(7)), nbf = Some(now)),
      JWTDefaultClaims(Some(application_id), Some(account_id), Some(session_id)),
      Map()
    )
  }

  def getJWTToken(jwt: JWT):String = {

    require(algorithm != null, "Algorithm not set")
    require(secret != null, "Secret not set")

    val headerJson = jwt.header.copy(alg = algorithm.name).toJson.toString
    val headerB64String = Base64.getUrlEncoder.encodeToString(headerJson.getBytes(charset))

    val reservedClaimsJson = jwt.reserved.toJson
    val defaulClaimsJson = jwt.default.toJson
    val payloadMap = jwt.payload

    val allClaims = reservedClaimsJson.asJsObject.fields ++ defaulClaimsJson.asJsObject.fields ++ payloadMap.map(p => {(p._1, JsString(p._2))})
    val claimsB64String = Base64.getUrlEncoder.encodeToString(JsObject(allClaims).toString.getBytes(charset))

    val prefix = headerB64String + "." + claimsB64String

    val signature = getB64Signature(prefix)

    prefix + "." + signature
  }

  def getSignature(payload: String): Array[Byte] = {
    algorithm match {
      case HS256 | HS384 | HS512 => {
        val mac: Mac = Mac.getInstance(algorithm.value)
        mac.init(new SecretKeySpec(secret.getBytes, algorithm.value))
        mac.doFinal(payload.getBytes(charset))
      }
      case NONE => "".getBytes(charset)
    }
  }

  def getB64Signature(payload: String): String = {
    Base64.getUrlEncoder.encodeToString(getSignature(payload))
  }

  def validateJWTToken(jwt: String): Try[JWT] = {
    Try {
      // Extract the various parts of a JWT
      val parts: (String, String, String) = jwt.split('.') match {
        case Array(header, payload, signature) => (header, payload, signature)
        case Array(header, payload) => (header, payload, "")
        case _ => throw new IllegalArgumentException("JWT could not be split into a header, payload, and signature")
      }

      val headerB64    = parts._1
      val payloadB64   = parts._2
      val signatureB64 = parts._3

      val prefix = headerB64 + "." + payloadB64
      val expectedB64Signature = getB64Signature(prefix)

      require(signatureB64 == expectedB64Signature, "Signature Does not Match")

      val headerStr: String = new String(Base64.getUrlDecoder.decode(headerB64.getBytes(charset)))
      val payloadStr: String = new String(Base64.getUrlDecoder.decode(payloadB64.getBytes(charset)))

      val jwtHeader: JWTHeader = headerStr.parseJson.convertTo[JWTHeader]
      val expectedAlg = Algorithm(jwtHeader.alg)

      require(expectedAlg == algorithm, "JWT has a different algorithm from expected")

      val payloadFields = payloadStr.parseJson.asJsObject.fields

      var jWTReservedClaims = JWTReservedClaims()
      var jWTDefaultClaims = JWTDefaultClaims()

      val payloadFinal = payloadFields.filter(x => {
        x._1 match {
          case s if s == "exp" => {
            jWTReservedClaims = jWTReservedClaims.copy(exp = Some(x._2.convertTo[DateTime]))
            false
          }
          case s if s == "nbf" => {
            jWTReservedClaims = jWTReservedClaims.copy(nbf = Some(x._2.convertTo[DateTime]))
            false
          }
          case s if s == "iat" => {
            jWTReservedClaims = jWTReservedClaims.copy(iat = Some(x._2.convertTo[DateTime]))
            false
          }
          case s if s == "iss" => {
            jWTReservedClaims = jWTReservedClaims.copy(iss = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "aud" => {
            jWTReservedClaims = jWTReservedClaims.copy(aud = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "prn" => {
            jWTReservedClaims = jWTReservedClaims.copy(prn = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "jti" => {
            jWTReservedClaims = jWTReservedClaims.copy(jti = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "typ" => {
            jWTReservedClaims = jWTReservedClaims.copy(typ = Some(x._2.convertTo[String]))
            false
          }
          case _ => true
        }
      }).filter(x => {
        x._1 match {
          case s if s == "application_id" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(application_id = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "account_id" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(account_id = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "session_id" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(session_id = Some(x._2.convertTo[String]))
            false
          }
          case _ => true
        }
      }).map(x => (x._1, x._2.convertTo[String]))

      JWT(jwtHeader, jWTReservedClaims, jWTDefaultClaims, payloadFinal)
    }
  }
}

trait JWTWithKey extends JWTSupport with KeySupport with KaredoConstants {
  val ks = getKeyStore(keyStoreName, keyStoreType, keyStorePass)
  val secret = getSecret(ks, keyAlias, keyStorePass)
  setSecret(secret)
  setAlgorithm(HS256)
}

