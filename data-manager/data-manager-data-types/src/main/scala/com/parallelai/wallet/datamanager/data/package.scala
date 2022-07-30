package com.parallelai.wallet.datamanager

import java.util.UUID

package object data {

  type DeviceID = UUID
  type UserID = UUID


  def applicationIdFromString(appId: String) : DeviceID = UUID.fromString(appId)
  def userIdFromString(userId: String) : DeviceID = UUID.fromString(userId)
}
