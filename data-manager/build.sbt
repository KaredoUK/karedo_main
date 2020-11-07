import org.slf4j.LoggerFactory
import pakkio.JenkinsJUnitXmlTestsListener
import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

name := "data-manager"

Common.settings

test in assembly := {}

javacOptions in ThisBuild ++= Seq("-source", "1.6")

lazy val common = (project.in(file("./common" ))).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val data = (project.in(file("./data-manager-data-types" ))).dependsOn(common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val model = (project.in(file("./model")).dependsOn(common)).dependsOn(common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val api = (project.in(file("./data-manager-api"))).dependsOn(data,model,common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*).settings(assemblySettings: _*)
//lazy val web = (project.in(file("./data-manager-web"))).enablePlugins(PlayScala).dependsOn(data,model,common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

lazy val root = project.in( file(".") )
  .aggregate(data, api, model)


testOptions in Test += Tests.Setup( () => Embedder.startMongo)

testOptions in Test += Tests.Cleanup( () => println("After Tests"))

testListeners <<= target.map(t => Seq(new JenkinsJUnitXmlTestsListener(t.getAbsolutePath)))


TaskKey[Unit]("start-mongo") := Embedder.startMongo

TaskKey[Unit]("stop-mongo") := Embedder.stopMongo

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"