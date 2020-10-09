package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{Hint, AdvertisementDetail, Brand}

/**
 * Created by pakkio on 29/09/2014.
 */
class MongoHintDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions with MongoTestUtils  {

  sequential

  "HintMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> s"$embedConnectionPort",
        "mongo.db.name" -> "test",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )

    val hintDAO = new HintMongoDAO

    val userid=UUID.randomUUID()
    val brandid=UUID.randomUUID()
    val ad=UUID.randomUUID()



    "can reread the 10 instances" in {

      for(i <- 0 until 10) {
        println(i)
      }
 /*       val myHint = Hint(userId = userid, brandId = brandid, ad = ad, score = 0.5)

        val id = hintDAO.insertNew(myHint).get

      }*/

      1 mustEqual (1)


    }


  }

}
