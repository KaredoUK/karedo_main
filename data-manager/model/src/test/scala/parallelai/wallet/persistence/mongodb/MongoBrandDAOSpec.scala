package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.ValidBSONType.DBObject
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
        "mongo.server.port" -> s"$embedConnectionPort",
        "mongo.db.name" -> "test",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )

    val brandDAO = new BrandMongoDAO

    def cleanBrands() = brandDAO.mongoClient.getDB("wallet_data").getCollection("Brand").remove(MongoDBObject.empty)

    lazy val mybrand = Brand(name = "brand X", iconId= UUID.randomUUID(), ads=List[AdvertisementMetadata]() )

    "create and retrieve a brand with a generated id " in {

      val id = brandDAO.insertNew(mybrand).get

      val findAfterInsert = brandDAO.getById(id).get

      findAfterInsert shouldEqual mybrand

      println("Passed 1")

      true

    }

    "can delete one instance" in {

      val id = brandDAO.insertNew(mybrand).get

      brandDAO.delete(id)

      val findAfterDelete = brandDAO.getById(id)

      findAfterDelete == None

    }

    "can delete all instances" in {
      cleanBrands()

      val id = brandDAO.insertNew(mybrand).get

      val list=brandDAO.list

      list should have size 1

      list.map( brand => brandDAO.delete(id))

      val list2=brandDAO.list

      list2 should have size 0

    }

  }

}
