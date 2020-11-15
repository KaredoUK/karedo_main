import sbt.Keys._
import sbt._
import sbtassembly.Plugin._
import AssemblyKeys._


name := "data-manager"

Common.settings

test in assembly := {}

javacOptions in ThisBuild ++= Seq("-source", "1.6")


exportJars := true

lazy val common = (project.in(file("./common" ))).
  settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val data = (project.in(file("./data-manager-data-types" ))).
  dependsOn(common).
  settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val model = (project.in(file("./model")).
  dependsOn(common)).dependsOn(common).
  settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val api = (project.in(file("./data-manager-api"))).
  dependsOn(data,model,common).
  settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*).
  settings(assemblySettings: _*) /*.
  settings(mainClass in assembly := Some("Rest")).
  settings(jarName in assembly := "Karedo.jar")*/

//lazy val web = (project.in(file("./data-manager-web"))).enablePlugins(PlayScala).dependsOn(data,model,common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

lazy val root = project.in( file(".") )
  .aggregate(data, api, model)

// be sure that Mongo is started on port 12345 before tests
testOptions in Test += Tests.Setup( () => Embedder.startMongo)

//testOptions in Test += Tests.Cleanup( () => println("After Tests"))


// http://dispatch.databinder.net/Dispatch.html allows for easier http/rest calls
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"



