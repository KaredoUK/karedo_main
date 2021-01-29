package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import parallelai.wallet.entity._
import parallelai.wallet.persistence.{LogDAO, BrandDAO}


class LogMongoDAO (implicit val bindingModule: BindingModule)
  extends LogDAO with MongoConnection with Injectable   {

  //This should be removed and using the MongoAppSupport features
  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[KaredoLog, UUID](collection = db("Log")) {}

  //def byId(id: UUID) = MongoDBObject("_id" -> id)


  //override def getById(id: UUID): Option[KaredoLog] = dao.findOneById(id)

  override def addLog(log: KaredoLog): Option[UUID] =  {

    val result = dao.insert( log )
    result
  }



}
