package com.parallelai.wallet.datamanager

import java.util.UUID

package object data {

  type ApplicationID = UUID
  type UserID = UUID


  def applicationIdFromString(appId: String) : ApplicationID = UUID.fromString(appId)
  def userIdFromString(userId: String) : ApplicationID = UUID.fromString(userId)
}
