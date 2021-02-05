package parallelai.wallet.entity

/**
 * These are the types we are finding on the DAO level
 */

import java.util.UUID

import com.novus.salat.annotations.Key
import org.joda.time.DateTime

case class KaredoSales(@Key("_id") id: UUID = UUID.randomUUID(),
                       userId: UUID,
                       adId: UUID,
                       code: String,
                       date: DateTime=new DateTime(),
                       dateExpires: DateTime=new DateTime().plusDays(3),
                       dateConsumed: Option[DateTime] = None
                        )
