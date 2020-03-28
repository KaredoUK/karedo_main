
name := "offer-manager"

Common.settings

javacOptions in ThisBuild ++= Seq("-source", "1.6")

lazy val api = project.in(file("./offer-manager-api"))
lazy val web = project.in(file("./offer-manager-web"))

lazy val root = project.in( file(".") )
  .aggregate(api, web)
