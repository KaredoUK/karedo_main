package karedo.routes.profile

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.profile.{Kar189_postProfile_actor, post_ChangePasswordActor}
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar189_postProfile extends KaredoRoute
  with Kar189_postProfile_actor {

  def route = {
    Route {

      // POST /account/{{account_id}}/profile
      path("account" / Segment / "profile") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[Kar189Req]) {
                  request =>
                    doCall({
                      exec(accountId, deviceId, request)
                    }
                    )
                }
              }
          }
      }
    }
  }
}

object post_ChangePassword extends KaredoRoute
  with post_ChangePasswordActor {

  def route = {
    Route {
      path("password" ) {
        post {
          entity(as[ChangePasswordRequest]) {
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
