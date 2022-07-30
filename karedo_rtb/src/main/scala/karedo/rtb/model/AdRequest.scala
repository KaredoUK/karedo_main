package karedo.rtb.model

import java.util.UUID

/**
  * Created by crajah on 03/09/2016.
  */
case class AdRequest(userId:UUID, count:Int)
case class AdResponse(ads:List[AdUnit])
case class AdUnit()