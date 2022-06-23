package karedo.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import karedo.entity._
import karedo.entity.dao

import karedo.persistence.{Interaction, BrandInteractionsDAO, HintDAO}

import scala.collection.immutable.Nil

object brandMongoUtils {
  def byId(brandId: UUID) = MongoDBObject("_id" -> brandId)
  def byInteractionId(interactionId: UUID) = "interactions" $elemMatch MongoDBObject("id" -> interactionId)
  def intById(id: UUID) = MongoDBObject("interactions.id" -> id)
}
import brandMongoUtils._

class BrandInteractionsMongoDAO (implicit val bindingModule: BindingModule)
  extends BrandInteractionsDAO
  with MongoConnection
  with Injectable
{

  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[MongoBrand,UUID](collection = db(Collections.BRAND)) {}


  override def clear() = {
    dao.collection.remove(MongoDBObject.empty)
  }

  override def insertNew(brandId: UUID, interaction: Interaction): Unit =  {

    dao.update(
      byId(brandId),
      $push(
        "interactions" -> grater[Interaction].asDBObject(interaction)
      )
    )
  }

  override def getById(interactionId: UUID): Option[Interaction] =
    dao.findOne(byInteractionId(interactionId)) flatMap {
      brand =>
        brand.interactions.find(_.id == interactionId)

    }

  override def getInteractions(brandId: UUID) = {
    val obrand = dao.findOne(byId(brandId))
    if (obrand == None) Nil
    else obrand.get.interactions
  }


  override def delete(interactionId: UUID): Unit = {
    dao.update(intById(interactionId), $pull("interactions" -> MongoDBObject("id" -> interactionId)))
    None
  }
}

