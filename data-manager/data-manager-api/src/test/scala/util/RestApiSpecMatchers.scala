package util

import java.util.UUID

import org.apache.commons.lang.StringUtils
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationLike
import parallelai.wallet.entity.{ClientApplication, UserAccount}
import spray.http.{HttpResponse, StatusCode}

trait RestApiSpecMatchers {
  self : SpecificationLike =>

  val beInactive: Matcher[ClientApplication] =
    ({ app: ClientApplication => ! app.active },
      "App should be inactive" )

  val haveActivationCode: Matcher[ClientApplication] =
    ({ app: ClientApplication => StringUtils.isNotEmpty(app.activationCode) },
      "App should have a validation code" )

  def beAnAppWithId(deviceId: UUID): Matcher[ClientApplication] =
    ({ app: ClientApplication => app.id == deviceId },
      s"App should have an deviceId == $applicationId" )

  def haveMsisdn(msisdn: String): Matcher[UserAccount] =
    ({user: UserAccount => user.msisdn == Some(msisdn) },
      s"User msisdn should be Some($msisdn)")

  def haveStatusCode(statusCode: StatusCode): Matcher[HttpResponse] =
    ({response: HttpResponse => response.status == statusCode },
      s"HttpResponse should have status code $statusCode")
}
