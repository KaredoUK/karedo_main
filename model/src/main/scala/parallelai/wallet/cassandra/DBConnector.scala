package parallelai.wallet.cassandra

import scala.concurrent. { blocking, Future }
import com.datastax.driver.core.{ Cluster, Session }
import com.newzly.phantom.Implicits._

object DBConnector {
  // TODO: Get table name from config file
  val keySpace = "parallelai_wallet"

  lazy val cluster =  Cluster.builder()
    // TODO: Get host and port from config file
    .addContactPoint("localhost")
    .withPort(9042)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

  lazy val session = blocking {
    cluster.connect(keySpace)
  }

  sys.addShutdownHook(session.close())
}

trait DBConnector {
  self: CassandraTable[_, _] =>

  def createTable(): Future[Unit] ={
    create.future() map (_ => ())
  }

  implicit lazy val cassandra: Session = DBConnector.session
}
