package karedo.rtb.util

/**
  * Created by crajah on 02/12/2016.
  */

trait DeviceMakes {
  val DEVICE_MAKE_IOS = "iOS"
  val DEVICE_MAKE_ANDROID = "Android"
}

sealed trait DeviceMake
case object DEV_TYPE_IOS extends DeviceMake
case object DEV_TYPE_ANDROID extends DeviceMake

