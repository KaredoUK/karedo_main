package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.get_VerifyEmailActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object get_VerifyEmailRoute extends KaredoRoute
  with get_VerifyEmailActor {

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

