package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar138Actor_Login

/**
  * Created by pakkio on 10/3/16.
  */
object Kar138_Login extends KaredoRoute
  with Kar138Actor_Login {

  def route = {
    Route {

      // POST /account/{{account_id}}/application/{{application_id}}/login
      path("account" / Segment / "application" / Segment / "login") {
        (accountId, applicationId) =>
          post {
            entity(as[Kar138Req]) {
              request =>
                doCall({
                  exec(accountId, applicationId, request)
                }
                )
            }
          }
      }
    }
  }
}
