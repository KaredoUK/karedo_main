package com.parallelai.wallet.datamanager.data

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}
import spray.json.DefaultJsonProtocol

import scala.annotation.meta.field


trait AccountAds extends DefaultJsonProtocol {


  @ApiModel(description = "Account suggested Offers (request)")
  case class AccountSuggestedOffersRequest
  (
    @(ApiModelProperty@field)(value = "deviceId in md5 format")
    deviceId: String = "",
    @(ApiModelProperty@field)(value = "session UUID as returned by previous calls")
    sessionId: String = "") extends ApiDataRequest
  implicit val accountGetadsRequest = jsonFormat2(AccountSuggestedOffersRequest)

  @ApiModel(description = "Account suggested Offers (response)")
  case class AccountSuggestedOffersResponse
  (
    @(ApiModelProperty@field)(value = "returned sessionId")
    sessionId: String = "",
    @(ApiModelProperty@field)(value = "List of URL to ads")
    urls: List[String] = List()) extends ApiDataResponse
  implicit val accountGetadsResponse = jsonFormat2(AccountSuggestedOffersResponse)

  // example values for [PROTOTYPE]
  val fixedSessionId = "ca8201bd-c9ea-42e7-ad16-4a7427f769cc"
  val fixedSessionId2 = "28850c9a-276a-44a5-b773-7b74f1afcfc2"
  val fixedAccountId = "adf959bd-d591-441b-931c-fcd426c4d923"
  val fixedDevIdMd5 = "cea099a8f5ac3e289e317d461beb9261"
  val fixedListAds = List("http//web.site/url1", "http://web.site/url2", "http://web.site/url3")

}
