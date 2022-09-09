package karedo.actors.url

import akka.http.scaladsl.model.headers
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UrlAccess, UserAccount, UserApp}
import karedo.util._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pakkio on 10/8/16.
  */


trait UrlMagic extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[UrlMagic])

  def exec(url_code: String, account_hash:String, isShareUrl:Boolean  = false): Result[Error, APIResponse] = {
    Try [Result[Error, APIResponse]] {
      val outUrl:String = dbUrlMagic.find(url_code) match {
        case OK(urlMagic) => {
          if(isShareUrl) if(checkIfFirst) urlMagic.first_url else urlMagic.second_url.getOrElse(url_magic_fallback_url)
          else urlMagic.first_url
        }
        case KO(_) => url_magic_fallback_url
      }

      Future {
        val account_id = dbHashedAccount.find(account_hash) match {
          case OK(hashedAccount) => hashedAccount.account_id
          case KO(_) => account_hash
        }

        dbUserUrlAccess.insertNew(UrlAccess(account_id = account_id, access_url = outUrl ))
      }

      OK(APIResponse(msg = "", code = HTTP_REDIRECT_302, mime = MIME_HTML, headers = List(headers.Location(outUrl))))
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_ERROR(f)
    }
  }

  def checkIfFirst(): Boolean = true

}