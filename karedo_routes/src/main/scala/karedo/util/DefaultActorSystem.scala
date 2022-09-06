package karedo.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
  * Created by gerrit on 8/15/16.
  */
trait DefaultActorSystem {
  implicit val actor_system = ActorSystem("server")
  implicit val actor_materializer = ActorMaterializer()

}
