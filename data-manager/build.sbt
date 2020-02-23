name := "data-manager"

Common.settings

lazy val data = project in file("./data-manager-data-types" )
lazy val api = project.in(file("./data-manager-api")).dependsOn(data)
lazy val web = project.in(file("./data-manager-web")).dependsOn(data)

lazy val root = project.in( file(".") )
  .aggregate(data,api, web)
