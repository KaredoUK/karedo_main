package karedo.routes.profile

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.profile.{post_ProfileActor, post_ChangePasswordActor}
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object post_ProfileRoute extends KaredoRoute
  with post_ProfileActor {

  def route = {
    Route {

      // POST /account/{{account_id}}/profile
      path("account" / Segment / "profile") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[post_ProfileRequest]) {
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

object post_ChangePasswordRoute extends KaredoRoute
  with post_ChangePasswordActor {

  def route = {
    Route {
      path("password" ) {
        post {
          entity(as[post_ChangePasswordRequest]) {
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
