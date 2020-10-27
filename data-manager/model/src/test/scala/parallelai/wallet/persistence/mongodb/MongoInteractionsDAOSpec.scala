package parallelai.wallet.persistence.mongodb

import java.util.UUID

//import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.NewBindingModule._
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.Hint

/**
 * Tests interactions
 */
class MongoInteractionsDAOSpec extends Specification
    with NoTimeConversions
    with MongoTestUtils {
  implicit val bindingModule = newBindingModuleWithConfig(
    Map(
      "mongo.server.host" -> "localhost",
      "mongo.server.port" -> "27017",
      "mongo.db.name" -> "test",
      "mongo.db.user" -> "",
      "mongo.db.pwd" -> ""
    )
  )

  val hintDAO = new InteractionsMongoDAO
  val userId = UUID.randomUUID()
  val brandId = UUID.randomUUID()
  val brandId2 = UUID.randomUUID()
  val adId = UUID.randomUUID()

  hintDAO.clear()



  sequential

  "JustBasic" should {
    "reread the initial instances" in {
      1 == 1
    }
  }

}
