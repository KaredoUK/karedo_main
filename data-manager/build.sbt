
name := "data-manager"

Common.settings

javacOptions in ThisBuild ++= Seq("-source", "1.6")

lazy val common = project.in(file("./common" ))
lazy val data = project.in(file("./data-manager-data-types" ))
lazy val model = project.in(file("./model")).dependsOn(common)
lazy val api = project.in(file("./data-manager-api")).dependsOn(data,model)
lazy val web = project.in(file("./data-manager-web")).dependsOn(data,model)


lazy val root = project.in( file(".") )
  .aggregate(data,api, web)
