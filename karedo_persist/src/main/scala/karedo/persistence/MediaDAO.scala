package karedo.persistence

import java.util.UUID

import karedo.entity.{MediaContentDescriptor, MediaContent}

trait MediaDAO {

  def createNew(newContent: MediaContent): String
  def findById(id: String): Option[MediaContent]
  def findByName(name: String): Option[MediaContent]
  def delete(id: String): Unit
  def list: List[MediaContentDescriptor]
}
