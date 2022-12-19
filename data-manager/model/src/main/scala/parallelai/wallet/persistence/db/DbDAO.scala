package parallelai.wallet.persistence.db

import java.util.UUID

import com.mongodb.WriteResult

import scala.util.Try

trait DbDAO[T<:AnyRef] {
  def insertNew(id:UUID,r:T): Try[Option[UUID]]
  def getById(id:UUID): Option[T]
  def update(id:UUID,r:T): Try[WriteResult]
  def delete(id:UUID,r:T): Try[WriteResult]
  def deleteAll()
}




