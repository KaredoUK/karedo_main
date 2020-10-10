package parallelai.wallet.persistence.mongodb

import java.util.UUID

import org.specs2.execute.AsResult
import org.specs2.matcher.MatchResult

//import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{Hint, AdvertisementDetail, Brand}

/**
 * Created by pakkio on 29/09/2014.
 */
class MongoHintDAOSpec extends Specification with NoTimeConversions with MongoTestUtils  {

  implicit val bindingModule = newBindingModuleWithConfig(
    Map(
      "mongo.server.host" -> "localhost",
      "mongo.server.port" -> "27017",
      "mongo.db.name" -> "test",
      "mongo.db.user" -> "",
      "mongo.db.pwd" -> ""
    )
  )
  val hintDAO = new HintMongoDAO
  val userId = UUID.randomUUID()
  val brandId = UUID.randomUUID()
  val brandId2 = UUID.randomUUID()
  val adId = UUID.randomUUID()

  hintDAO.clear()
  for (i <- 0 until 10) {
    val id = hintDAO.insertNew(Hint(userId = userId, brandId = brandId, ad = adId, score = 0.1*i)).get
  }
  for (i <- 0 until 3) {
    val id = hintDAO.insertNew(Hint(userId = userId, brandId = brandId2, ad = adId, score = 0.5*i)).get
  }


  sequential

  "JustBasic" should {
    "reread the initial instances" in {
      hintDAO.count mustEqual (13)
    }
  }
  "not existent userid, brand" should {
    "find empty list for strange userid" in {
      check(UUID.randomUUID(), UUID.randomUUID(), asked = 10, expected = 0)
    }
  }
  "with an existent user with 10 hints" should {

    "find 10 hints for first user and 10 asked" in {

      check(userId, brandId, asked = 10, expected = 10)
    }
    "find 10 hints for first user and 20 asked" in {
      check(userId, brandId, asked = 20, expected = 10)
    }
    "find 5 hints for first user and 5 asked" in {
      check(userId,brandId,asked=5,expected=5)
    }
  }
  "with an existent user with 3 hints" should {
    "find 3 hints for first user and 10 asked" in {
      check(userId, brandId2, asked = 10, expected = 3)
    }
  }


  def check(user:UUID,brand:UUID,asked:Int,expected:Int) = {
    hintDAO.suggestedNAdsForUserAndBrandLimited(user,brand,asked) must have size(expected)

  }
}
