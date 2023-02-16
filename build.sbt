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

lazy val karedo_routes = project
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "karedo_routes")
  .dependsOn(karedo_rtb)

lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
lazy val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
lazy val dispatchV = "0.11.3"
lazy val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchV

lazy val karedo_rtb = project
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "karedo_rtb")
  .enablePlugins(ScalaxbPlugin)
//  .settings(inThisBuild(List(
//    organization  := "com.example",
//    scalaVersion  := "2.11.8"
//  )))
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
    // scalaxbPackageNames in (Compile, scalaxb)    := Map(uri("http://schemas.microsoft.com/2003/10/Serialization/") -> "microsoft.serialization"),
    // logLevel in (Compile, scalaxb) := Level.Debug
  )
  .dependsOn(karedo_persist)

lazy val karedo_persist = project
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "karedo_persist")
  .dependsOn(salat)

lazy val salat = project
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "salat")

lazy val root = (project in file("."))
  .aggregate(salat, karedo_persist, karedo_rtb, karedo_routes)
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)

parallelExecution in Test := false
//parallelExecution in jacoco.Config := false

coverageEnabled := false
test in assembly := {}

