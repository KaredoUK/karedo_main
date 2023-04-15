package test.jwt

import common.AllTests
import karedo.common.jwt._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success, Try}
import karedo.common.misc.Util.now
import org.joda.time.DateTime

/**
  * Created by charaj on 11/03/2017.
  */
@RunWith(classOf[JUnitRunner])
class JWTTester extends AllTests with JWTWithKey {

  "JWT tester" should {
    val jwtOrig = JWT(
      JWTHeader(),
      JWTReservedClaims(Some(now), Some(now), Some(now)),
      JWTDefaultClaims(Some("app_id"), Some("acc_id"), Some("sess_id")),
      Map("test" -> "test")
    )

    "* create signature" in {
      val jwtToken = getJWTToken(jwtOrig)
      println(jwtToken)
      val jwtReceived = validateJWTToken(jwtToken) match {
        case Success(s) => s
        case Failure(f) => throw f
      }

      jwtReceived.header shouldEqual(jwtOrig.header)
      println(s"${jwtReceived.reserved}\n${jwtOrig.reserved}")
      jwtReceived.reserved shouldEqual(jwtOrig.reserved)
      jwtReceived.default shouldEqual(jwtOrig.default)
      jwtReceived.payload shouldEqual(jwtOrig.payload)

    }

    "* full test" in {
      val orig_jwt = JWT(
        header = JWTHeader(
          alg = "HS256",
          typ = "JWT"
        ),
        reserved = JWTReservedClaims(
          exp = Some(now)
        , nbf = Some(now)
        , iat = Some(now)
        , iss = Some("ISS")
        , aud = Some("AUD")
        , prn = Some("PRN")
        , jti = Some("JTI")
        , typ = Some("TYP")
        ),
        default = JWTDefaultClaims
        (
          application_id = Some("APP_ID")
        , account_id = Some("ACC_ID")
        , session_id = Some("SES_ID")
        , verify_id = Some("VER_ID")
        , context_id = Some("CTX_ID")
        , isVerified = Some(true)
        , isValidated = Some(false)
        , isLive = Some(true)
        , _any_json = Some("{\"a\"=\"b\"}")
        ),
        payload = Map("a" -> "b", "c" -> "d")
      )

      val jwtToken = getJWTToken(orig_jwt)
      println(jwtToken)
      val jwtReceived = validateJWTToken(jwtToken) match {
        case Success(s) => s
        case Failure(f) => throw f
      }

      jwtReceived shouldEqual orig_jwt
    }
  }
}
