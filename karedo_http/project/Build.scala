import sbt._
import Keys._

object MyBuild extends Build {
  
  lazy val root = project.in(file(".")).aggregate(routes, persist)

  lazy val routes = RootProject(file("../karedo_routes"))
  lazy val persist = RootProject(file("../karedo_persist"))
}
