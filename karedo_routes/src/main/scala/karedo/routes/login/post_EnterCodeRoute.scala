package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.post_EnterCodeActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object post_EnterCodeRoute extends KaredoRoute
  with post_EnterCodeActor {

  def route = {
    Route {

      // POST /verify?e={email}&c={email_code}&a={account_id}
      path("verify" ) {
        post {
          entity(as[post_EnterCodeRequest]) {
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

