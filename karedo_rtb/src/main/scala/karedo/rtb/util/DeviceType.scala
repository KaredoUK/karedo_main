package karedo.rtb.util

/**
  * Created by crajah on 02/12/2016.
  */

trait DeviceMakes {
  val DEVICE_MAKE_IOS = "iOS"
  val DEVICE_MAKE_ANDROID = "Android"
}

sealed trait DeviceMake
case object iOS extends DeviceMake
case object Android extends DeviceMake

