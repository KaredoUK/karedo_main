package karedo.routes.url

import akka.http.scaladsl.model.{RemoteAddress, headers}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.UrlAccess
import karedo.routes.KaredoRoute
import karedo.util._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import scala.concurrent.ExecutionContext.Implicits.global

object get_UrlMagicShareRoute extends KaredoRoute
  with UrlMagicActor {

  def route = {
    Route {
      path("shr" ) {
        optionalHeaderValueByName("User-Agent") {
          ua =>
            extractClientIP {
              ip =>
                get {
                  parameters('u, 'v) {
                    (url_code, hash_account) =>
                      doCall({
                        exec(url_code, hash_account, true, ua, ip)
                      }
                      )
                  }
                }
            }
        }
      }

    }

  }
}

object get_UrlMagicNormalRoute extends KaredoRoute
  with UrlMagicActor {

  def route = {
    Route {
      path("nrm" ) {
        optionalHeaderValueByName("User-Agent") {
          ua =>
            extractClientIP {
              ip =>
                get {
                  parameters('u, 'v) {
                    (url_code, hash_account) =>
                      doCall({
                        exec(url_code, hash_account, false, ua, ip)
                      }
                      )
                  }
                }
            }
        }
      }

    }

  }
}


trait UrlMagicActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[UrlMagicActor])

  def exec(url_code: String, account_hash:String, isShareUrl:Boolean  = false, ua: Option[String] = None, ip: RemoteAddress): Result[Error, APIResponse] = {
    Try [Result[Error, APIResponse]] {
      val outUrl:String = dbUrlMagic.find(url_code) match {
        case OK(urlMagic) => {
          if(isShareUrl) if(checkIfFirst(ua, ip)) urlMagic.first_url else urlMagic.second_url.getOrElse(url_magic_fallback_url)
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
      case Failure(f) => MAKE_THROWN_ERROR(f)
    }
  }

  def checkIfFirst(ua: Option[String] = None, ip: RemoteAddress): Boolean = {
    ua match {
      case None => false
      case Some(u) => {
        if(u.contains("facebookexternalhit") || u.contains("Google-HTTP-Java-Client") || u.contains("Yahoo!")) true else false
      }
    }
  }
}
