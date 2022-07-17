package com.parallelai.wallet.datamanager.data

import spray.json.DefaultJsonProtocol


trait AccountAds extends DefaultJsonProtocol {


  case class AccountGetadsRequest(accountId: String = "", deviceId: String = "", sessionId: String = "")
  implicit val accountGetadsRequest = jsonFormat3(AccountGetadsRequest)
  case class AccountGetadsResponse(accountId: String = "", deviceId: String = "", sessionId: String = "", ads: List[String] = List())
  implicit val accountGetadsResponse = jsonFormat4(AccountGetadsResponse)

}
