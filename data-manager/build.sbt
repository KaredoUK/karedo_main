
name := "data-manager"

Common.settings

javacOptions in ThisBuild ++= Seq("-source", "1.6")

lazy val common = (project.in(file("./common" ))).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val data = (project.in(file("./data-manager-data-types" ))).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val model = (project.in(file("./model")).dependsOn(common)).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val api = (project.in(file("./data-manager-api"))).enablePlugins(PlayScala).dependsOn(data,model).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
lazy val web = (project.in(file("./data-manager-web"))).dependsOn(data,model).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)


lazy val root = project.in( file(".") )
  .aggregate(data,api, web)
