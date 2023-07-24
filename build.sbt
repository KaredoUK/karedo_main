import sbt.Keys.{publishTo, _}
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.Version.Bump.Next
import sbtrelease._
import com.typesafe.sbt.SbtScalariform._
import com.amazonaws.regions.{Region, Regions}

import scalariform.formatter.preferences._
import de.heikoseeberger.sbtheader.{HeaderPattern, HeaderPlugin}
import sbtassembly.AssemblyPlugin.autoImport.PathList

import scala.language.postfixOps

// For Karedo
resolvers ++= Seq[Resolver](
  s3resolver.value("Parallel AI S3 Releases resolver", s3("release.repo.parallelai.com")),
  s3resolver.value("Parallel AI S3 Snapshots resolver", s3("snapshot.repo.parallelai.com"))
)

publishMavenStyle := false
s3region := Region.getRegion(Regions.EU_WEST_2)
publishTo := {
  val prefix = if (isSnapshot.value) "snapshot" else "release"
  Some(s3resolver.value("Parallel AI "+prefix+" S3 bucket", s3(prefix+".repo.parallelai.com")) withIvyPatterns)
}


// Common Settings
lazy val commonSettings = Seq(
  organization := "karedo",
  name := "karedo",
  version := "0.0.5-SNAPSHOT",
  scalaVersion := "2.12.2",
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
  // Scalariform settings
/*
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveDanglingCloseParenthesis, true),
*/
  // Header settings
  HeaderPlugin.autoImport.headers := Map(
    "scala" -> (
      HeaderPattern.cStyleBlockComment,
      """|/*
         | * Copyright © 2016 Karedo Limited. All rights reserved.
         | * No information contained herein may be reproduced or transmitted in any form
         | * or by any means without the express written permission of Karedo Limited.
         | */
         |
         |""".stripMargin
    ),
    "conf" -> (
      HeaderPattern.hashLineComment,
      """|# Copyright © 2016 Karedo Limited. All rights reserved.
         |# No information contained herein may be reproduced or transmitted in any form
         |# or by any means without the express written permission of Karedo Limited.
         |
         |""".stripMargin
    )
  ),
  resolvers ++= Seq(
    "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
    , Resolver.mavenLocal
    , Resolver.sbtPluginRepo("releases")
    , Resolver.sonatypeRepo("public")
    , Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
//    , "jitpack.io" at "https://jitpack.io"
//    , Resolver.bintrayIvyRepo("hajile", "maven")
//    , "Artima Maven Repository" at "http://repo.artima.com/releases"
  ),
  assemblyMergeStrategy in assembly := {
    case PathList("io.netty", xs @ _*)         => MergeStrategy.first
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case s if s.endsWith("io.netty.versions.properties") => MergeStrategy.first
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
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

lazy val libs_guice = "com.google.inject" % "guice" % "3.0"

lazy val libs_macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
lazy val libs_scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test


// Library Definitions
lazy val libs_test = Seq(
  "org.specs2" %% "specs2-core" % "3.9.5" % "test",
  "org.specs2" %% "specs2-junit" % "3.9.5" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

lazy val akkaV = "2.5.4"
lazy val akkaHttpV = "10.0.10"
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

lazy val dispatchV = "0.13.1"
lazy val libs_dispatch = Seq("net.databinder.dispatch" %% "dispatch-core" % dispatchV)

lazy val salatV = "1.11.2"
lazy val libs_salat = Seq(
  "com.github.salat" %% "salat" % salatV
)

lazy val zxingV = "3.3.0"
lazy val libs_zxing = Seq(
  "com.google.zxing" % "core" % zxingV,
  "com.google.zxing" % "javase" % zxingV,
  "com.github.kenglxn.qrgen" % "javase" % "2.2.0"
)

lazy val scalaxV = "0.4.9"
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

lazy val blueprints_version = "2.5.0"
lazy val rexster_version = "2.5.0"
lazy val mongodb_java_driver_version = "2.9.1"
lazy val libs_graph = Seq(
  "org.mongodb" % "mongo-java-driver" % mongodb_java_driver_version,
  "com.tinkerpop.blueprints" % "blueprints-core" % blueprints_version,
  "com.tinkerpop.blueprints" % "blueprints-test" % blueprints_version % "test",
  "com.tinkerpop.rexster" % "rexster-core" % rexster_version,
  "com.foursquare" % "fongo" % "1.1.1"
)

lazy val libs_reactivemongo = Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.12.6"
)

lazy val libs_K8ServiceLocator = Seq(
  "com.lightbend" %% "lagom-service-locator-dns" % "1.0.3-SNAPSHOT"
//  "ru.smslv.akka" %% "akka-dns" % "2.4.2"
)

//lazy val akka_dns = (project in file("akka-dns"))
//lazy val service_locator_dns = (project in file("service-locator-dns"))
//    .dependsOn(akka_dns)

/*
// Project Definitions
lazy val srv_account = (project in file("srv_account"))
  .settings(commonSettings: _*)
  .settings(name := "srv_account")
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
//      lagomScaladslPersistenceCassandra,
//      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      libs_macwire,
      libs_scalaTest
    ) ++ libs_reactivemongo ++ libs_akka
    ++ libs_K8ServiceLocator
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(karedo_reactive)
  .dependsOn(karedo_common)
//  .dependsOn(service_locator_dns)
*/

/*
lazy val srv_profile = (project in file("srv_profile"))
  .settings(commonSettings: _*)
  .settings(name := "srv_profile")
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      //      lagomScaladslPersistenceCassandra,
      //      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      libs_macwire,
      libs_scalaTest
    ) ++ libs_reactivemongo ++ libs_akka
      ++ libs_K8ServiceLocator
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(karedo_reactive)
  .dependsOn(karedo_common)
//  .dependsOn(service_locator_dns)
*/



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
          "org.clapper" %% "classutil" % "1.1.2",
          "io.igl" %% "jwt" % "1.2.2"
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
//        libs_scalaXml ++
        libs_test ++
        libs_akka ++
        libs_logging ++
        libs_config ++
        Seq(
          "org.jsoup" % "jsoup" % "1.10.3",
          "com.google.guava" % "guava" % "23.0"
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
//  .dependsOn(karedo_graph)


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
//        libs_scalax ++
        libs_test ++
        libs_akka ++
        libs_joda ++
        libs_nimbusds ++
        libs_reactivemongo
//        ++
//        libs_reactivemongo
  )

// ########### Reactve Mongo #############
lazy val karedo_reactive = (project in file("karedo_reactive"))
  .settings(commonSettings: _*)
  .settings(releaseSettings: _*)
  .settings(name := "reactive_mongo")
  .settings(
    libraryDependencies ++=
      libs_logging ++
//        libs_scalax ++
        libs_test ++
        libs_akka ++
        libs_joda ++
        libs_nimbusds ++
        libs_reactivemongo
  )


// ########### Graph #############
//lazy val karedo_graph = (project in file("karedo_graph"))
//  .settings(commonSettings: _*)
//  .settings(releaseSettings: _*)
//  .settings(name := "graph")
//  .settings(
//    libraryDependencies ++= libs_graph
//  )


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
  .settings(
    libraryDependencies ++= Seq(
      jdbc,
      ehcache,
      ws,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.1" % Test,
      libs_guice
    )
  )
//  .settings(fork in run := true)
  .dependsOn(karedo_common)
  .dependsOn(karedo_persist)
  .dependsOn(karedo_rtb)

// ########### Root #############
lazy val root = (project in file("."))
  //  .dependsOn(salat, karedo_persist, karedo_rtb, karedo_routes)
  .aggregate(salat, karedo_persist, karedo_rtb, karedo_routes, karedo_feeder)//, srv_account, srv_profile)
//  .settings(commonSettings: _*)
//  .settings(releaseSettings: _*)



//parallelExecution in Test := false
//parallelExecution in jacoco.Config := false

//coverageEnabled := false
//test in assembly := {}
mainClass in(Compile, run) := Some("karedo.Main")

