package karedo.util

import org.joda.time.DateTime

/**
  * Created by charaj on 07/03/2017.
  */
trait JWTSupport {

  trait JWTReservedClaims {
    var exp: Option[DateTime] = None        // Expiry
    var nbf: Option[DateTime] = None        // Not Before
    var iat: Option[DateTime] = None        // Issued At
    var iss: Option[String] = None          // Issuer
    var aud: Option[String] = None          // Audience
    var prn: Option[String] = None          // Prinicpal
    var jti: Option[String] = None          // JWT ID
    var typ: Option[String] = None           // Type of Contents
  }

  trait JWTBasePayload {
    var application_id: String = ""
    var account_id: String = ""
    var session_id: Option[String] = None
  }

  case class JWTHeader
  (
//    alg: String = "HS256",
    typ: String = "JWT"
  )

  case class JWTPayload() extends JWTReservedClaims with JWTBasePayload


  sealed trait Algorithm {
    def name: String
    override def toString = name
  }

  case object HS256 extends Algorithm { def name = "HS256" }
  case object HS384 extends Algorithm { def name = "HS384" }
  case object HS512 extends Algorithm { def name = "HS512" }
  case object NONE extends Algorithm { def name = "NONE" }
  case object UNKNOWN extends Algorithm { def name = "UNKNOWN" }

  object Algorithm {
    def apply(name: String): Algorithm = {
      name match {
        case s if s == HS256.name => HS256
        case s if s == HS384.name => HS384
        case s if s == HS512.name => HS512
        case s if s == NONE.name => NONE
        case _ => UNKNOWN
      }
    }
  }

}

