package karedo.persistence

import java.util.UUID

import karedo.entity.{Hint, SuggestedAdForUsersAndBrandModel}

/**
 * Created by pakkio on 29/09/2014.
 */
trait HintDAO {
  def clear(): Unit
  def insertNew(hint: Hint) : Option[UUID]
  def suggestedNAdsForUserAndBrandLimited(user: UUID, brand: UUID, number:Int): List[Hint]
  def count: Long

}


