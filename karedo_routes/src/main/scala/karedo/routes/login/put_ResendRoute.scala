package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login._
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object put_ResendRoute extends KaredoRoute
  with put_ResendActor {

  def route = {
    Route {
      path("resend") {
          put {
            entity(as[put_ResendRequest]) {
              request =>
                doCall({
                  exec(request)
                }
                )
            }
          }
      }
    }
  }
}



object put_ResendEmailRoute extends KaredoRoute
  with put_ResendEmailActor {

  def route = {
    Route {
      path("resend" / "email") {
        put {
          entity(as[put_ResendEmailRequest]) {
            request =>
              doCall({
                exec(request)
              }
              )
          }
        }
      }
    }
  }
}

object post_ValidateEmailRoute extends KaredoRoute
  with post_ValidateEmailActor {

  def route = {
    Route {
      path("validate" / "email") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            post {
              entity(as[post_ValidateEmailRequest]) {
                request =>
                  doCall({
                    exec(deviceId, request)
                  }
                  )
              }
            }
        }
      }
    }
  }
}

object get_ValidateSessionRoute extends KaredoRoute
  with get_ValidateSessionActor {

  def route = {
    Route {
      path("validate" / "session") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            get {
              parameters( 'a, 'p, 's ? ) {
                (account_id, application_id, session_id) =>
                  doCall({
                    exec(deviceId, account_id, application_id, session_id)
                  })
              }
            }
        }
      }
    }
  }
}
