package parallelai.wallet.persistence.mongodb

import java.util.UUID

import org.specs2.mutable.{Before, Specification}
import parallelai.wallet.entity.Hint


class MongoHintDAOSpec
  extends Specification
  with MongoTestUtils
{

  val hintDAO=new HintMongoDAO()
  val userId = UUID.randomUUID()
  val brandId = UUID.randomUUID()
  val brandId2 = UUID.randomUUID()
  val adId = UUID.randomUUID()

  initialize

  sequential

  "initialization" should {
    "have produces enough instances to play on" in {
      hintDAO.count mustEqual (13)
    }
  }
  "asking for not existent userid, brand" should {
    "find empty list for strange userid" in {
      check(UUID.randomUUID(), UUID.randomUUID(), asked = 10, expected = 0)
    }
  }
  "asking for an existent user with 10 hints" should {

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


  private def initialize: Any = {
    hintDAO.clear()
    for (i <- 0 until 10) {
      val id = hintDAO.insertNew(Hint(userId = userId, brandId = brandId, ad = adId, score = 0.1 * i)).get
    }
    for (i <- 0 until 3) {
      val id = hintDAO.insertNew(Hint(userId = userId, brandId = brandId2, ad = adId, score = 0.5 * i)).get
    }
  }
  private def check(user:UUID,brand:UUID,asked:Int,expected:Int) = {
    hintDAO.suggestedNAdsForUserAndBrandLimited(user,brand,asked) must have size(expected)

  }
}
