package core

import org.specs2.mutable._

/**
  * Created by pakkio on 9/16/16.
  */
object Twirl extends Specification {
  "Twirl" should {
    "resolve a html template" in {
      val render = core.html.activation("*secretcode*", "*urlwheretogo*")
      render.toString mustEqual
        """
          |<html>
          |Welcome to Karedo, your activation code is *secretcode*.<br>
          |Please click on <a href="*urlwheretogo*">this link</a>
          |</html>""".stripMargin
    }
    "resolve a txt template" in {
      val render = core.txt.activation("*secretcode*", "*urlwheretogo*")
      render.toString mustEqual
        """Welcome to Karedo, your activation code is *secretcode*. Please click on *urlwheretogo*""".stripMargin
    }

  }

}
