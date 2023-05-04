import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.Version.Bump.Next
import sbtrelease._

// Common Settings
lazy val commonSettings = Seq(
  organization := "karedo",
  name := "karedo",
  version := "0.0.3-SNAPSHOT",
  scalaVersion := "2.11.8",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  parallelExecution in Test := false,
  coverageEnabled := false,
  test in assembly := {},
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-Xlint",
    "-Ywarn-dead-code",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-Yrangepos"
  ),
  //  initialize := {
  //    val _ = initialize.value
  //    if (sys.props("java.specification.version") != "1.8")
  //      sys.error("Java 8 is required for this project.")
  //  },
  resolvers ++= Seq(
    "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.mavenLocal,
    Resolver.sbtPluginRepo("releases"),
    Resolver.sonatypeRepo("public"),
    Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns),
    "jitpack.io" at "https://jitpack.io"
  )


)

// Release Settings.
lazy val releaseSettings = Seq(
  releaseVersionBump := Next,
  releaseTagName := s"${(version in ThisBuild).value}",
  releaseNextVersion := { ver => Version(ver).map(_.bump.string).getOrElse(versionFormatError) },
  releaseProcess := Seq[ReleaseStep](
    inquireVersions,
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

// Library Definitions
lazy val libs_test = Seq(
  "org.specs2" %% "specs2-core" % "3.8.5" % "test",
  "org.specs2" %% "specs2-junit" % "3.8.5.1" % "test",
  "junit" % "junit" % "4.8.1" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

lazy val akkaV = "2.5.1"
lazy val akkaHttpV = "10.0.6"
lazy val libs_akka = Seq(
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,
  "com.typesafe.akka" %% "akka-parsing" % akkaHttpV,

  "com.typesafe.akka" %% "akka-agent" % akkaV,
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "com.typesafe.akka" %% "akka-typed" % akkaV,
  "com.typesafe.akka" %% "akka-contrib" % akkaV
)

lazy val libs_config = Seq(
  "com.typesafe" % "config" % "1.3.1"
)

lazy val libs_logging = Seq(
  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.10"
)

lazy val libs_scalaXml = Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
)

lazy val dispatchV = "0.11.3"
lazy val libs_dispatch = Seq("net.databinder.dispatch" %% "dispatch-core" % dispatchV)

lazy val salatV = "1.11.0"
lazy val libs_salat = Seq(
  "com.github.salat" %% "salat" % "1.11.0"
)

lazy val zxingV = "3.3.0"
lazy val libs_zxing = Seq(
  "com.google.zxing" % "core" % zxingV,
  "com.google.zxing" % "javase" % zxingV,
  "com.github.kenglxn.qrgen" % "javase" % "2.2.0"
)

lazy val scalaxV = "0.4.3"
lazy val libs_scalax = Seq(
  "com.github.scala-incubator.io" %% "scala-io-core" % scalaxV,
  "com.github.scala-incubator.io" %% "scala-io-file" % scalaxV
)

lazy val jodaV = "2.9.9"
lazy val libs_joda = Seq(
  "joda-time" % "joda-time" % jodaV
)

lazy val libs_nimbusds = Seq(
  "com.nimbusds" % "nimbus-jose-jwt" % "4.37.1"
)


// Project Definitions
// ########### Routes #############
lazy val karedo_routes = (project in file("karedo_routes"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "routes")
  .settings(SbtTwirl.projectSettings)
  .settings(
    libraryDependencies ++=
      libs_test ++
        libs_akka ++
        libs_logging ++
        libs_config ++
        libs_zxing ++
        Seq(
          "org.clapper" %% "classutil" % "1.0.11",
          "io.igl" %% "jwt" % "1.2.0"
        )
  )
//  .settings(
//    libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
//  )
//  .settings(appengineSettings: _*)
  .dependsOn(karedo_common)
  .dependsOn(karedo_rtb)


// ########### RTB #############
lazy val karedo_rtb = (project in file("karedo_rtb"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "rtb")
  .enablePlugins(ScalaxbPlugin)
  .settings(
    scalaxbDispatchVersion in(Compile, scalaxb) := dispatchV,
    scalaxbPackageName in(Compile, scalaxb) := "generated",
    libraryDependencies ++=
      libs_dispatch ++
        libs_scalaXml ++
        libs_test ++
        libs_akka ++
        libs_logging ++
        libs_config ++
        Seq(
          "org.jsoup" % "jsoup" % "1.10.1",
          "com.google.guava" % "guava" % "21.0"
        )
  )
  .dependsOn(karedo_common)
  .dependsOn(karedo_persist)

// ########### Persist #############
lazy val karedo_persist = (project in file("karedo_persist"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "persist")
  .settings(
    libraryDependencies ++=
      libs_salat ++
        libs_test ++
        libs_akka ++
        libs_logging ++
        libs_config
  )
  .dependsOn(karedo_common)
  .dependsOn(karedo_graph)


// ########### Salat #############
lazy val salat = (project in file("salat"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
//  .settings(name := "salat")


// ########### Common #############
lazy val karedo_common = (project in file("karedo_common"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "common")
  .settings(
    libraryDependencies ++=
      libs_logging ++
        libs_scalax ++
        libs_test ++
        libs_akka ++
        libs_joda ++
        libs_nimbusds
  )

// ########### Graph #############
lazy val karedo_graph = (project in file("karedo_graph"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "graph")


// ########### Config #############
lazy val karedo_config = (project in file("karedo_config"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "config")


// ########### Feeder #############
lazy val karedo_feeder = (project in file("karedo_feeder"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "feeder")
  .enablePlugins(PlayScala)
  .dependsOn(karedo_common)
  .dependsOn(karedo_persist)
  .dependsOn(karedo_rtb)

// ########### Root #############
lazy val root = (project in file("."))
  //  .dependsOn(salat, karedo_persist, karedo_rtb, karedo_routes)
  .aggregate(salat, karedo_persist, karedo_rtb, karedo_routes, karedo_feeder)
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      jdbc,
      cache,
      ws,
      "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
    )
  )
  .settings(fork in run := true)



//parallelExecution in Test := false
//parallelExecution in jacoco.Config := false

//coverageEnabled := false
//test in assembly := {}
mainClass in(Compile, run) := Some("karedo.Main")

