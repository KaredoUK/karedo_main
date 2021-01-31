package parallelai.wallet.persistence.mongodb


import java.util.UUID

import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.config.PropertiesConfigPropertySource
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{ClientApplication, UserAccount, AdvertisementDetail, Brand}
import parallelai.wallet.persistence.BrandInteractionsDAO
import scala.collection.immutable.Map

/**
 * Created by pakkio on 16/12/2014.
 */
trait TestWithLocalMongoDb extends MongoTestUtils with NoTimeConversions {

  lazy val defaultBindingConfig =
    Map(
      "mongo.server.host" -> "127.0.0.1", //192.168.149.138",
      "mongo.server.port" -> "12345", //27017",
      "mongo.db.name" -> "test",
      "mongo.db.user" -> "",
      "mongo.db.pwd" -> ""
    )


  implicit val bindingModule = newBindingModuleWithConfig  (
    PropertiesConfigPropertySource(defaultBindingConfig)
  )


  // allocates DAO for db handling
  val hintDAO = new HintMongoDAO
  val brandDAO = new BrandMongoDAO
  val mediaDao = new MongoMediaDAO
  val brandInteractionsDAO = new BrandInteractionsMongoDAO
  val logDAO = new LogMongoDAO()

  // allocate some initial objects to be used in testings
  val userId = UUID.randomUUID()
  val brandId = UUID.randomUUID()
  val brandId2 = UUID.randomUUID()
  val adId = UUID.randomUUID()



  def newbrand = Brand(name = "brand X", iconId= "iconId", ads=List[AdvertisementDetail]() )

  val accountDAO = new UserAccountMongoDAO

  val userAccount = UserAccount(UUID.randomUUID(), Some("12345678"), Some("user@email.com"))
  val clientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")
  val activeAccount = UserAccount(UUID.randomUUID(), Some("87654321"), Some("other.user@email.com"), active = true)
  val activeClientApplication = ClientApplication(UUID.randomUUID(), activeAccount.id, "ACT_CODE_1", active = true)

  clearAll()

  def clearAll() ={
    //println("cleaning database")
    // drops the test database to be sure it is really empty
    hintDAO.dao.collection.getDB.dropDatabase()

  }
}
