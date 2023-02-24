import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.Version.Bump.Next
import sbtrelease.{Version, versionFormatError}

lazy val commonSettings = Seq(
  organization := "karedo",
  name := "karedo",
  version := "0.0.2-SNAPSHOT",
  scalaVersion := "2.11.8"
)

lazy val releaseSettings = Seq(
  releaseVersionBump := Next,
  releaseTagName := s"${(version in ThisBuild).value}",
  releaseNextVersion := { ver => Version(ver).map(_.bump.string).getOrElse(versionFormatError) }
)

lazy val karedo_routes = (project in file("karedo_routes"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "routes")
  .dependsOn(karedo_rtb)

lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
lazy val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
lazy val dispatchV = "0.11.3"
lazy val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchV

lazy val karedo_rtb = (project in file("karedo_rtb"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "rtb")
  .enablePlugins(ScalaxbPlugin)
  .settings(
    name          := "foo-project",
    libraryDependencies ++= Seq(dispatch),
    libraryDependencies ++= {
      if (scalaVersion.value startsWith "2.10") Seq()
      else Seq(scalaXml, scalaParser)
    })
  .settings(
    scalaxbDispatchVersion in (Compile, scalaxb) := dispatchV,
    scalaxbPackageName in (Compile, scalaxb)     := "generated"
  )
  .dependsOn(karedo_persist)

lazy val karedo_persist = (project in file("karedo_persist"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "persist")
  .dependsOn(salat)

lazy val salat = (project in file("salat"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "salat")

lazy val karedo_common = (project in file("karedo_common"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "common")

lazy val root = (project in file("."))
  .dependsOn(salat, karedo_persist, karedo_rtb, karedo_routes)
  .aggregate(salat, karedo_persist, karedo_rtb, karedo_routes)
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)

parallelExecution in Test := false
//parallelExecution in jacoco.Config := false

coverageEnabled := false
test in assembly := {}

