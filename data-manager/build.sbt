
name := "data-manager"

Common.settings

javacOptions in ThisBuild ++= Seq("-source", "1.6")

lazy val common = (project.in(file("./common" ))).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val data = (project.in(file("./data-manager-data-types" ))).dependsOn(common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val model = (project.in(file("./model")).dependsOn(common)).dependsOn(common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val api = (project.in(file("./data-manager-api"))).dependsOn(data,model,common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*).settings(assemblySettings: _*)
lazy val web = (project.in(file("./data-manager-web"))).enablePlugins(PlayScala).dependsOn(data,model,common).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)


lazy val root = project.in( file(".") )
  .aggregate(data,api, web)
