package karedo

import karedo.entity.{DbPrefs, DbUserAd}
import karedo.util.Configurable

/**
  * Created by pakkio on 13/10/16.
  */
object Preload extends Configurable {


  val prefs = new DbPrefs {}
  val rows = prefs.preload()
  println(s"DbPrefs done loading $rows")

  val ads = new DbUserAd {}
  println(s"DbUserAd done loading $rows")


}
