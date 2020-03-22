import parallelai.wallet.persistence.cassandra.{UserAccountRecord, ClientApplicationRecord, ClientApplicationCassandraDAO, UserAccountCassandraDAO}
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.Future._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object Setup extends App {


  val userAccountCassandraPersistence = new UserAccountCassandraDAO

  println("Creating cassandra tables")

  val tables = userAccountCassandraPersistence.createTables

  println(s"Waiting for ${tables.size} to be created")

  Await.ready( sequence( tables ), 20.seconds )

  println("Done")

  userAccountCassandraPersistence.clientApplicationRecord.cassandra.close()

}
