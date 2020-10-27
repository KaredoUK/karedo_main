package parallelai.wallet.persistence.mongodb

import java.util.UUID

/**
 * Created by pakkio on 11/12/2014.
 */
trait InteractionsDAO {
  def clear():Unit
  def insertNew(hint: Interaction): Option[UUID]
}
