package parallelai.wallet.persistence.mongodb

import com.escalatesoft.subcut.inject.config.PropertiesConfigMapSource
import scala.concurrent.Future
import scala.concurrent.Await._
import scala.concurrent.duration._

trait MongoTestUtils {
  implicit def mapAsConfigSource : Map[String, String] => PropertiesConfigMapSource = new PropertiesConfigMapSource(_)

  def fromFuture[T] (future: Future[T]) : T = result(future, 2.seconds)

}
