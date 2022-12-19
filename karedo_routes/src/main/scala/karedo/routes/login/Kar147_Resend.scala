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
object Kar147_Resend extends KaredoRoute
  with Kar147_Resend_actor {

  def route = {
    Route {
      path("resend") {
          put {
            entity(as[Kar147_Resend]) {
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



object Kar147_ResendEmail extends KaredoRoute
  with Kar147_ResendEmail_actor {

  def route = {
    Route {
      path("resend" / "email") {
        put {
          entity(as[Kar147_ResendEmail]) {
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

object Kar147_ValidateEmail extends KaredoRoute
  with Kar147_ValidateEmail_actor {

  def route = {
    Route {
      path("validate" / "email") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            post {
              entity(as[Kar147_ValidateEmail_Request]) {
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

object Kar147_ValidateSession extends KaredoRoute
  with Kar147_ValidateSession_actor {

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
