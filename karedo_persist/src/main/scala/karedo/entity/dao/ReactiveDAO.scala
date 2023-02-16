package karedo.entity.dao

import scala.util.Try
import scala.concurrent.Future

/**
  * Created by charaj on 04/04/2017.
  */
trait ReactiveDAO[K, T <: Keyable[K]] {
  def findOne(id: K): Future[Try[T]]
  def findAll(): Future[Try[List[T]]]
  def ids: Future[Try[List[K]]]
  def insert(doc: T): Future[Try[T]]
  def update(doc: T): Future[Try[T]]
  def upsert(doc: T): Future[Try[T]]
  def delete(doc: T): Future[Try[T]]
  def deleteAll(): Future[Try[Unit]]

  def lock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts", max: Int):  Future[Try[T]]
  def unlock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts"): Future[Try[T]]
}
