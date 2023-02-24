package test.jwt

import common.AllTests
import karedo.jwt._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success, Try}
import karedo.util.Util.now
import karedo.util._

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
  }
}
