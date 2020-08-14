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
class MongoBrandDAOSpec extends Specification with NoTimeConversions with MongoTestUtils  {

  sequential




  "BrandMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> "27017",
        "mongo.db.name" -> "wallet_data",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )


    val brandDAO = new MongoBrandDAO

    def cleanbrands = brandDAO.mongoClient.getDB("wallet_data").getCollection("Brand").remove(MongoDBObject.empty)


    def mybrand=Brand(name = "brand X", iconPath="iconpath",ads=List[AdvertisementMetadata]() )

    "create and retrieve a brand with a generated id " in {


      val insert =
        brandDAO.insertNew(mybrand)

      val findAfterInsert = brandDAO.getById(insert.id).get

      findAfterInsert shouldEqual insert

      println("Passed 1")

      true

    }

    "can delete one instance" in {

      val insert = brandDAO.insertNew(mybrand)


      brandDAO.delete(insert.id)


      val findAfterDelete = brandDAO.getById(insert.id)




      findAfterDelete == None

    }

    "can delete all instances" in {

      cleanbrands

      val insert = brandDAO.insertNew(mybrand)

     // Thread.sleep(500)

      val list=brandDAO.list

      list.size shouldEqual( 1 )

      list.map( brand => brandDAO.delete(brand.id))

      val list2=brandDAO.list

      list.size shouldEqual( 0 )



    }

  }

}
