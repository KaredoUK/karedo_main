package karedo.common.jwt

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.common.joda.DateTimeJsonHelper._
import karedo.common.misc.Util._
import karedo.route.util._
import karedo.common.crypto._
import org.joda.time.DateTime
import spray.json._


import scala.util.Try

case class JWTHeader
(
  alg: String = "HS256",
  typ: String = "JWT"
)

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

case class JWTDefaultClaims
(
  application_id: Option[String] = None
  , account_id: Option[String] = None
  , session_id: Option[String] = None
  , verify_id: Option[String] = None
  , context_id: Option[String] = None
  , isVerified: Option[Boolean] = None
  , isValidated: Option[Boolean] = None
  , isLive: Option[Boolean] = None
  , _any_json: Option[String] = None
)

case class JWT
(
  header: JWTHeader
  , reserved: JWTReservedClaims
  , default: JWTDefaultClaims
  , payload: Map[String, String]
)


protected trait JWTSupport
  extends DefaultJsonProtocol
  with SprayJsonSupport with WithCrypto
{
  implicit val json_JWTHeader = jsonFormat2(JWTHeader)
  implicit val json_JWTReservedClaims = jsonFormat8(JWTReservedClaims)
  implicit val json_JWTPayload = jsonFormat9(JWTDefaultClaims)
  implicit val json_JWT = jsonFormat4(JWT)


  def getDefaultJWTToken(default: JWTDefaultClaims) = {
    getJWTToken(getDefaultJWT(default))
  }

  def getDefaultJWT(default: JWTDefaultClaims) = {
    JWT(
      JWTHeader(),
      getDefaultReservedClaims,
      default,
      Map()
    )
  }

  private def getDefaultReservedClaims(): JWTReservedClaims = {
    JWTReservedClaims(
      exp = Some(now.plusDays(7)),
      nbf = Some(now),
      iat = Some(now),
      iss = Some("Karedo"),
      aud = Some("Karedo"),
      prn = Some("karedo.co.uk"),
      jti = Some(newUUID),
      typ = Some("_private_")
    )
  }

  def getJWTToken(jwt: JWT):String = {

    val headerJson = jwt.header.copy(alg = getAlgorithm.name).toJson.toString
    val headerB64String = Base64.getUrlEncoder.encodeToString(headerJson.getBytes(getCharset))

    val reservedClaimsJson = jwt.reserved.toJson
    val defaulClaimsJson = jwt.default.toJson
    val payloadMap = jwt.payload

    val allClaims = reservedClaimsJson.asJsObject.fields ++ defaulClaimsJson.asJsObject.fields ++ payloadMap.map(p => {(p._1, JsString(p._2))})
    val claimsB64String = Base64.getUrlEncoder.encodeToString(JsObject(allClaims).toString.getBytes(getCharset))

    val prefix = headerB64String + "." + claimsB64String

    val signature = getB64Signature(prefix)

    prefix + "." + signature
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

      val headerStr: String = new String(Base64.getUrlDecoder.decode(headerB64.getBytes(getCharset)))
      val payloadStr: String = new String(Base64.getUrlDecoder.decode(payloadB64.getBytes(getCharset)))

      val jwtHeader: JWTHeader = headerStr.parseJson.convertTo[JWTHeader]
      val expectedAlg = Algorithm(jwtHeader.alg)

      require(expectedAlg == getAlgorithm, "JWT has a different algorithm from expected")

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
          case s if s == "verify_id" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(verify_id = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "context_id" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(context_id = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "_any_json" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(_any_json = Some(x._2.convertTo[String]))
            false
          }
          case s if s == "isVerified" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(isVerified = Some(x._2.convertTo[Boolean]))
            false
          }
          case s if s == "isValidated" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(isValidated = Some(x._2.convertTo[Boolean]))
            false
          }
          case s if s == "isLive" => {
            jWTDefaultClaims = jWTDefaultClaims.copy(isLive = Some(x._2.convertTo[Boolean]))
            false
          }
          case _ => true
        }
      }).map(x => (x._1, x._2.convertTo[String]))

      JWT(jwtHeader, jWTReservedClaims, jWTDefaultClaims, payloadFinal)
    }
  }
}

trait JWTWithKey extends JWTSupport with KeySupport {
  val secret = getDefaultSecret
  setSecret(secret)
  setAlgorithm(HS256)
}

trait JWTMechanic {
  val jwtMechanic = new JWTWithKey {}

  def getJWT(application_id: String, account_id: String
             , session_id: Option[String] = None
             , verify_id: Option[String] = None
             , context_id: Option[String] = None
            ) = Some(jwtMechanic.getDefaultJWT(JWTDefaultClaims(
    application_id = Some(application_id),
    account_id = Some(account_id),
    session_id = session_id,
    verify_id = verify_id,
    context_id = context_id
  )))
}
