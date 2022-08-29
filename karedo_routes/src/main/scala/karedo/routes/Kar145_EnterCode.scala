package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar145Actor_EnterCode

/**
  * Created by pakkio on 10/3/16.
  */
object Kar145_EnterCode extends KaredoRoute
  with Kar145Actor_EnterCode {

  def route = {
    Route {

      // POST /verify?e={email}&c={email_code}&a={account_id}
      path("verify" ) {
        post {
          entity(as[Kar145Req]) {
            request =>
              doCall(
                {
                  exec(request)
                }
              )

          }
        }
      }
    }
  }
}

