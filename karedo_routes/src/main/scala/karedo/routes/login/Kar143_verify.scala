package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.Kar143_verify_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar143_verify extends KaredoRoute
  with Kar143_verify_actor {

  def route = {
    Route {

      // GET /verify?e={email}&c={email_code}&a={account_id}
      path("verify" ) {
        get {
          parameters( 'e, 'c, 'v ? ) {
            ( email, email_code, verifyId ) =>
              doCall(
                {
                  exec(email, email_code, verifyId)
                }
              )

          }
        }
      }
    }
  }
}

