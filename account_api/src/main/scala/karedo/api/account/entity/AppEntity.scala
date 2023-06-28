package karedo.api.account.entity

import akka.Done
import reactivemongo.api.MongoConnectionOptions
import karedo.api.account.model.UserApp
import reactivemongo.api.{Collection, DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, document}

import scala.concurrent.{ExecutionContext, Future}
import karedo.api.account.model.UserApp._
import karedo.route.util.Configurable
import karedo.common.misc.Util._
import play.api.libs.json.{Format, Json}

import scala.concurrent.ExecutionContext.Implicits.global

//class AppEntity extends PersistentEntity {
//  val collectionName = "UserApp"
//  val userAppDAO = new MongoDAO[UserApp] { override def name = collectionName }
//
//  override type Command = ReplyType[_]
//  override type Event = AccountEvent
//  override type State = UserApp
//
//  override def initialState: State = UserApp(_id = newUUID)
//
//  override def behavior: Behavior = {
//    case userApp => Actions().onCommand[RegisterRequest, Done] {
//      case (request: RegisterRequest, ctx, state) =>
//        ctx.thenPersist(
//          UserAppChangedEvent(UserApp(_id = request.application_id,
//            account_id = newUUID))
//        ) { _ =>
//          ctx.reply(Done)
//        }
//    }
//  }
//}

object ConnectToMongo extends Configurable {
  val mongoUri = conf.getString("mongo.auth.uri")
  val databaseName = "test"

  println(mongoUri)

  private val ec = implicitly(ExecutionContext)

  // Connect to the database: Must be done only once per application
  private val driver = MongoDriver()
  private val conOpts = MongoConnectionOptions(sslEnabled = true, sslAllowsInvalidCert = true)
  private val parsedUri = MongoConnection.parseURI(mongoUri).map(p => p.copy(options = p.options.copy(sslEnabled = true, sslAllowsInvalidCert = true)))
  private val connection = parsedUri.map(driver.connection(_))

  private val futureConnection = Future.fromTry(connection)
  private def db: Future[DefaultDB] = futureConnection.flatMap(_.database(databaseName))

  private val tablePrefix = "TEST_ACCOUNT_"

  def getCollection(name: String) = db.map(_.collection(s"${tablePrefix}${name}"))
}

abstract class MongoKeyableEntity {
  def _id():String
}

abstract class MongoDAO[T <: MongoKeyableEntity] (implicit reader: BSONDocumentReader[T], writer: BSONDocumentWriter[T], ec: ExecutionContext) {
  def name: String

  def collection = ConnectToMongo.getCollection(name)

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