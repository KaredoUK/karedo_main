package karedo.rtb.model

import karedo.entity.{DbUserAccount, DbUserProfile}

/**
  * Created by crajah on 15/10/2016.
  */
trait DbCollections {
  val dbUserAccount = new DbUserAccount {}
  val dbUserProfile = new DbUserProfile {}

}
