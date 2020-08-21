package web

import com.escalatesoft.subcut.inject.Injectable
import core.{ServiceActors, CoreActors, Core}
import api.Api
import akka.io.IO
import spray.can.Http

/**
 * Provides the web server (spray-can) for the REST api in ``Api``, using the actor system
 * defined in ``Core``.
 *
 * You may sometimes wish to construct separate ``ActorSystem`` for the web server machinery.
 * However, for this simple application, we shall use the same ``ActorSystem`` for the
 * entire application.
 *
 * Benefits of separate ``ActorSystem`` include the ability to use completely different
 * configuration, especially when it comes to the threading model.
 */
trait Web {
  this: Api with ServiceActors with Core with Injectable =>

  val bindPort = injectOptionalProperty[Int]("service.port") getOrElse 8080
  val bindAddress = injectOptionalProperty[String]("service.bindAddress") getOrElse "0.0.0.0"

  IO(Http)(system) ! Http.Bind(rootService, bindAddress, port = bindPort)

}
