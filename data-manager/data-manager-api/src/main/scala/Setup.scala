import parallelai.wallet.persistence.cassandra.{UserAccountRecord, ClientApplicationRecord, ClientApplicationCassandraDAO, UserAccountCassandraDAO}
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.Future._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object Setup extends App {


  val userAccountCassandraPersistence = new UserAccountRecord
  val clientApplicationCassandraPersistence = new ClientApplicationRecord

  println("Creating cassandra tables")

  println(s"User account: ${userAccountCassandraPersistence.schema()}")
  println(s"Client app account: ${clientApplicationCassandraPersistence.schema()}")

  val userAccountTable = userAccountCassandraPersistence.createTable()
  val clientAppTable = clientApplicationCassandraPersistence.createTable()

  Await.ready( sequence( List(userAccountTable, clientAppTable) ), 30.seconds )
}
