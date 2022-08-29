package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.sample.Entities
import karedo.util.RouteDebug
//import org.clapper.classutil.ClassInfo
import org.slf4j.LoggerFactory

trait Routes
  extends Entities

    with RouteDebug {

  override val routes = Kar134.route ~ Kar135.route ~ Kar136.route ~ Kar166.route ~
    Kar169.route ~ Kar170.route ~ Kar171.route ~ Kar172.route ~ Kar188.route ~ Kar189.route ~
    Kar194.route ~ Kar195.route ~ Kar141_SendCode.route ~ Kar143.route ~ Kar145.route

//  override val routes = {
//    println("findAllRoutesExtendingKaredoRoute")
//    def companion[T](name : String)(implicit man: Manifest[T]) : T =
//      Class.forName(name).getField("MODULE$").get(null).asInstanceOf[T]
//
//    import org.clapper.classutil.ClassFinder
//    val rootRoute: Route = path("") {
//      get(complete("Karedo API version 0.0.2-SNAPSHOT"))
//    }
//    val finder = ClassFinder()
//    val classes = finder.getClasses()
//
//    val classesInfos = ClassFinder.concreteSubclasses("karedo.routes.KaredoRoute",classes)
//
//    classesInfos.foreach(println)
//
//    val routes =  classesInfos.toList map { x:ClassInfo =>
//      val obj:KaredoRoute = companion[KaredoRoute](x.name)
//      obj.route
//    }
//    val newRoutes = routes.foldLeft(rootRoute)(_~_)
//
//    println(newRoutes)
//
//    newRoutes
//
//  }

  override val logger = LoggerFactory.getLogger(classOf[Routes])

}


