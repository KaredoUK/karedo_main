package core

import java.net.URI

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import core.MessengerActor.SendMessage
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import akka.actor.{Props, ActorLogging, ActorRef, Actor}
import com.parallelai.wallet.datamanager.data._
import sun.management.jdp.JdpJmxPacket
import scala.concurrent.{ExecutionContext, Future}
import scala.async.Async._
import java.util.UUID
import spray.json._
import parallelai.wallet.entity._
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import scala.Some
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang.RandomStringUtils
import javax.management.InvalidApplicationException
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import scala.Some
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import scala.Some
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import scala.concurrent.Future.successful

/**
 * We use the companion object to hold all the messages that the ``RegistrationActor``
 * receives.
 */
object BrandActor {

  def props( brandDAO : BrandDAO)(implicit bindingModule : BindingModule) : Props =
    Props( classOf[BrandActor], brandDAO, bindingModule)
}

class BrandActor(brandDAO : BrandDAO)(implicit val bindingModule : BindingModule) extends Actor with ActorLogging with Injectable {

  def receive: Receive = {
    case request: BrandData =>  sender ! createBrand(request)
  }

  def createBrand(request: BrandData ): Either[BrandError,BrandResponse] =
     {
       validateBrand(request) match {
         case Some(error) => Left(BrandInvalidRequest(error))
         case _ =>
           log.info("Creating new brand for request {}", request)
           val newbrand = Brand(name = request.name, iconPath = request.iconPath, ads = List[AdvertisementMetadata]())
           brandDAO.insertNew(newbrand)
           Right(BrandResponse(newbrand.id))
       }
    }

  def validateBrand(brand: BrandData):Option[String] = {
    brand match {
      case BrandData("",_) => Some("Empty name not accepted")
      case BrandData(_,"") => Some("IconPath empty not accepted")
      case _ => None
    }

  }
}
