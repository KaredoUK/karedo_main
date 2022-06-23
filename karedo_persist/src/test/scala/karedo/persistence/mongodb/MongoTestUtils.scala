package karedo.persistence.mongodb

import scala.concurrent.Await._
import scala.concurrent.Future
import scala.concurrent.duration._

trait MongoTestUtils {
  implicit def mapAsConfigSource : Map[String, String] => PropertiesConfigMapSource = new PropertiesConfigMapSource(_)

  def fromFuture[T] (future: Future[T]) : T = result(future, 2.seconds)

  lazy val defaultBindingConfig =
    Map(
      "mongo.server.host" -> "127.0.0.1", //192.168.149.138",
      "mongo.server.port" -> "12345", //27017",
      "mongo.db.name" -> "test",
      "mongo.db.user" -> "",
      "mongo.db.pwd" -> ""
    )

  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()

  implicit val bindingModule = newBindingModuleWithConfig  (
    PropertiesConfigPropertySource(defaultBindingConfig)
  )

}
