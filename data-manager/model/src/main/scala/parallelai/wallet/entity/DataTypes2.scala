package parallelai.wallet.entity

/**
 * These are the types we are finding on the DAO level
 */

import java.util.UUID

import com.novus.salat.annotations.Key
import org.joda.time.DateTime
import parallelai.wallet.entity.KaredoTypes.KaredoPoints

case class KaredoSales(@Key("_id") id: UUID = UUID.randomUUID(),
                       saleType: String,
                       accountId: UUID,
                       adId: Option[UUID]=None,
                       code: Option[String]=None,
                       dateCreated: DateTime=new DateTime(),
                       dateExpires: DateTime=new DateTime().plusDays(30),
                       dateConsumed: Option[DateTime] = None,
                       points: KaredoPoints
                        )
