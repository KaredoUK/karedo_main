package karedo.common.mongo.reactive

import karedo.common.config.Configurable
import reactivemongo.api._
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

object ConnectToMongo extends Configurable {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val mongoUri = conf.getString("mongo.auth.uri")
  private val databaseName = conf.getString("mongo.db.name")
  private val isSSL = conf.getBoolean("mongo.ssl")

  println(mongoUri)

  //private val ec = implicitly(ExecutionContext)

  // Connect to the database: Must be done only once per application
  private val driver = MongoDriver()
  private val conOpts = MongoConnectionOptions(sslEnabled = true, sslAllowsInvalidCert = true)
  private val parsedUri = MongoConnection.parseURI(mongoUri).map(p => p.copy(options = p.options.copy(sslEnabled = isSSL, sslAllowsInvalidCert = true)))
  private val connection = parsedUri.map(driver.connection(_))

  private val futureConnection = Future.fromTry(connection)
  private def db: Future[DefaultDB] = futureConnection.flatMap(_.database(databaseName))

  def getCollection(name: String)(prefix: String) = db.map(_.collection(s"${prefix}${name}"))
}

abstract class MongoKeyableEntity {
  def _id():String
}

abstract class MongoDAO[T <: MongoKeyableEntity] (implicit reader: BSONDocumentReader[T], writer: BSONDocumentWriter[T], ec: ExecutionContext, prefix: String) {
  def collectionName: String

  val collection = ConnectToMongo.getCollection(collectionName)(prefix)

  def findOneById(_id: String): Future[Option[T]] = collection.flatMap(_.find(document("_id" -> _id)).one[T])

  def findFirstById(_id: String): Future[List[T]] = {
    collection.flatMap(
      _.find(
        document("_id" -> _id)
      )
      .cursor[T]()
        .collect[List]()
    )
  }

  def insert(item: T): Future[Unit] = collection.flatMap(_.insert(item).map(_ => {}))

  def update(item: T): Future[Int] = collection.flatMap((_.update(document("_id" -> item._id), item).map(_.n)))
}

abstract class JournaledMongoDAO[T <: MongoKeyableEntity](implicit reader: BSONDocumentReader[T], writer: BSONDocumentWriter[T], ec: ExecutionContext, prefix: String) extends MongoDAO[T] {
  def journal = ConnectToMongo.getCollection(collectionName + "_jrnl")(prefix)

  override def update(item: T): Future[Int] = {
    journal.flatMap(_.insert(item).map(_ => {}))

    super.update(item)
  }
}
