lazy val commonSettings = Seq(
 organization := "karedo",
 name := "karedo",
 version := "0.0.2-SNAPSHOT",
 scalaVersion := "2.11.8"
)

lazy val root = (project in file("."))
  .aggregate(salat, persist, rtb, routes)

lazy val salat = (project in file("salat"))
lazy val persist = (project in file("karedo_persist"))
  .settings(
   commonSettings
  )
  .dependsOn(salat)
lazy val rtb = (project in file("karedo_rtb"))
  .settings(
  commonSettings
 )
  .dependsOn(persist)

lazy val routes = (project in file("karedo_routes"))
  .settings(
  commonSettings
 )
  .dependsOn(persist, rtb)


//coverageEnabled := true

//lazy val root = project.in(file(".")).aggregate(db,rtb,routes)
//
//lazy val db = project.in(file("karedo_persist"))
//
//lazy val rtb = project.in(file("karedo_rtb")) dependsOn(db)
//
//lazy val routes = project.in(file("karedo_routes")) dependsOn(db, rtb)

run := {
 println("******************************")
 println("sbt run is disabled at top level")
 println("Please Use:")
 println("   1. $ build_and_run.sh")
 println("   2. $ cd karedo_routes; sbt run")
 println("******************************")
}