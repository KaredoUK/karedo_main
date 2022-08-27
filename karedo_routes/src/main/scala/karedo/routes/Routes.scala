package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.sample.Entities
import karedo.util.RouteDebug
import org.clapper.classutil.ClassInfo
import org.slf4j.LoggerFactory

trait Routes
  extends Entities

    with RouteDebug {

  override val routes = {
    println("findAllRoutesExtendingKaredoRoute")
    def companion[T](name : String)(implicit man: Manifest[T]) : T =
      Class.forName(name).getField("MODULE$").get(null).asInstanceOf[T]

    import org.clapper.classutil.ClassFinder
    val rootRoute: Route = path("") {
      get(complete("Karedo API version 0.0.2-SNAPSHOT"))
    }
    val classes = ClassFinder().getClasses()
    val classesInfos = ClassFinder.concreteSubclasses("karedo.routes.KaredoRoute",classes)
    val routes =  classesInfos.toList map { x:ClassInfo =>
      val obj:KaredoRoute = companion[KaredoRoute](x.name)
      obj.route
    }
    routes.foldLeft(rootRoute)(_~_)

  }

  override val logger = LoggerFactory.getLogger(classOf[Routes])

}


