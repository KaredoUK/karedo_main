package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{AdvertisementMetadata, Brand}
import com.escalatesoft.subcut.inject.NewBindingModule
import NewBindingModule._

/**
 * Created by pakkio on 29/09/2014.
 */
class MongoBrandDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions with MongoTestUtils  {

  sequential

  "BrandMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> "27017",
        "mongo.db.name" -> "wallet_db",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )

    val brandDAO = new MongoBrandDAO

    "create and retrieve a brand with a generated id " in {

      val id=UUID.randomUUID()

      val insert = fromFuture {

        brandDAO.insertNew(Brand(id, "brand X","iconpath", List[AdvertisementMetadata]() ))
      }
      val findAfterInsert = fromFuture(brandDAO.getById(insert.id))

      findAfterInsert shouldEqual insert

    }

  }

}
