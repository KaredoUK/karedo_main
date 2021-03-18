import sbt.Keys._
import sbt._
import sbtassembly.Plugin._
import AssemblyKeys._

scalaVersion in ThisBuild := "2.11.4"

name := "data-manager"

Common.settings

test in assembly := {}

javacOptions in ThisBuild ++= Seq("-source", "1.6")


exportJars := true

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }



lazy val common = (project.in(file("common" ))).
settings(//net.virtualvoid.sbt.graph.Plugin.graphSettings: _*,
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) })

lazy val data = (project.in(file("data-manager-data-types" ))).
dependsOn(common).
settings(//net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) })

lazy val model = (project.in(file("model")).
  dependsOn(common)).dependsOn(common).
settings(//net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) })





lazy val api = (project.in(file("data-manager-api"))).
  dependsOn(data,model,common).
  settings(//net.virtualvoid.sbt.graph.Plugin.graphSettings: _*).
    ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }).
  settings(assemblySettings: _*) /*.
  settings(mainClass in assembly := Some("Rest")).
  settings(jarName in assembly := "Karedo.jar")*/
lazy val it = (project.in(file("it"))).
  dependsOn(data,model,common,api).
  configs( IntegrationTest ).
  settings( Defaults.itSettings : _*)

//lazy val web = (project.in(file("./data-manager-web"))).enablePlugins(PlayScala).dependsOn(data,model,common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

lazy val root = project.in( file(".") )
  .aggregate(data, api, model)

// be sure that Mongo is started on port 12345 before tests
testOptions in Test += Tests.Setup( () => Embedder.startMongo)

//testOptions in Test += Tests.Cleanup( () => println("After Tests"))
testOptions in Test += Tests.Argument("junitxml", "console")

// http://dispatch.databinder.net/Dispatch.html allows for easier http/rest calls
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"



