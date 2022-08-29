package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar143Actor

/**
  * Created by pakkio on 10/3/16.
  */
object Kar143 extends KaredoRoute
  with Kar143Actor {

  def route = {
    Route {

      // GET /verify?e={email}&c={email_code}&a={account_id}
      path("verify" ) {
        get {
          parameters('e, 'c, 'a ?) {
            (email, email_code, accountId) =>
              doCall(
                {
                  exec(email, email_code, accountId)
                }
              )

          }
        }
      }
    }
  }
}

