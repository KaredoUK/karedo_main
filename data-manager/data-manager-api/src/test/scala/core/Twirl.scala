package core

import org.specs2.mutable._

/**
  * Created by pakkio on 9/16/16.
  */
object Twirl extends Specification {
  "Twirl" should {
    "resolve a template" in {
      val render = core.html.hello("Bob", 22)
      render.toString mustEqual
        """
          |<html>
          |
          |<h1>Welcome Bob!!</h1>
          |<p>You are 22 years old, <i>have a great evening !</i></p>
          |</html>""".stripMargin
    }

  }

}
