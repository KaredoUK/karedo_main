package utils

import org.specs2.mutable._
import karedo.common.result.{Result, OK, KO}

/**
  * Created by pakkio on 10/30/16.
  */
class TestResult extends Specification {
  case class Record(id: Int, desc: String)
  val RIGHT = OK(Record(1, "aaa"))
  val LEFT = KO("an error")
  def increment(value: Record) = {

    OK(value.copy(id=value.id+1))
  }

  "Result new class" should {
    "honor right class" in {

      if (RIGHT.isOK) {
        val rec = RIGHT.get
        rec.id mustEqual 1
        rec.desc mustEqual "aaa"
      } else
        ko("lucky value not recognized")

      RIGHT.isKO mustEqual false
      //a.err must throwA(new NoSuchElementException)
    }
    "honor left class" in {

      if(LEFT.isKO) {
        val rec = LEFT.err
        rec mustEqual "an error"

      }
      else ko("error value not recognized")
      LEFT.isOK mustEqual false
    }
    "can be used in a for comprehension with right biasing" in {

      val x = for {
        a <- RIGHT
        b <- increment(a)
        c <- increment(b)
      } yield c

      x.isOK mustEqual true
      x.get.id mustEqual 3
    }
    "can be used in a for comprehension with left error" in {

      val x = for {
        a <- LEFT
        b <- increment(a)
        c <- increment(b)
      } yield c

      x.isKO mustEqual true
      x.err mustEqual "an error"
    }
  }

}
