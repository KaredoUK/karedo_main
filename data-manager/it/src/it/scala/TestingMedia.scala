import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import org.specs2.mutable.Specification
import spray.httpx.UnsuccessfulResponseException

import scala.util.Random


class TestingMedia
  extends Specification
  with ItEnvironment {

  clearAll()

  sequential

  "Testing Media" should {


    "be able to Upload and download a media file" in {
      println("Testing media files")
      val r = RegisterAccount
      val originalB = Array[Byte](1, 2, 3, 4)
      val id = addMedia(r.sessionId, "name", "image/jpeg", originalB)
      //println("added media: "+id.mediaId)
      val hex = "[0-9a-f]+"
      id.mediaId must beMatching(hex)

      val b = getMedia(r.sessionId, id.mediaId)
      b should_== (originalB)

      implicit class ExtendedRandom(ran: scala.util.Random) {
        def nextByte = (ran.nextInt(256) - 128).toByte
      }
      val randomB = Array.fill(100)(scala.util.Random.nextByte)
      val id2 = addMedia(r.sessionId, "name2", "image/jpeg", randomB)
      id2.mediaId must beMatching(hex)

      val b2 = getMedia(r.sessionId, id2.mediaId)
      b2 should_== (randomB)

    }
  }


}
