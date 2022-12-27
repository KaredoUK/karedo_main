organization := "karedo"

name := "rtb"

version := "0.0.2-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += "spray repo" at "http://repo.spray.io"
resolvers += Resolver.mavenLocal
//resolvers += Resolver.typesafeRepo("releases")
//resolvers += Resolver.typesafeIvyRepo("releases")
resolvers += Resolver.sbtPluginRepo("releases")

/*
libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, add dependency on scala-xml module
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value ++ Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
        "org.scala-lang.modules" %% "scala-swing" % "1.0.1")
    case _ =>
      // or just libraryDependencies.value if you don't depend on scala-swing
      libraryDependencies.value :+ "org.scala-lang" % "scala-swing" % scalaVersion.value
  }
}
*/



scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.4.11"
  val sprayV = "1.3.2" //1.3.4 is latest but does not have build for spray-json
  val karedoV = "0.0.2-SNAPSHOT"
  Seq(
    "karedo" %% "persist" % karedoV,

//    "io.spray"            %%  "spray-can"         % sprayV,
//    "io.spray"            %%  "spray-json"        % sprayV,
//    "io.spray"            %%  "spray-client"      % sprayV,
//    "io.spray"            %%  "spray-http"        % sprayV,
//    "io.spray"            %%  "spray-httpx"       % sprayV,
//    "io.spray"            %%  "spray-routing"     % sprayV,
//    "io.spray"            %%  "spray-testkit"     % sprayV  % "test",

    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka"   %%  "akka-actor"        % akkaV,

    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",
    "com.typesafe.akka"   %%  "akka-testkit"      % akkaV   % "test",

    "ch.qos.logback" % "logback-classic" % "1.1.10",

    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.specs2" %% "specs2-core" % "3.8.5" % "test",
    "org.specs2" %% "specs2-junit" % "3.8.5.1" % "test",
    "junit" % "junit" % "4.8.1" % "test",
    "org.jsoup" % "jsoup" % "1.10.1",
    "com.google.guava" % "guava" % "21.0"
  )
}

// mainClass in (run) := Some("karedo.rtb.runner.BidRunner")

lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
lazy val scalaParser = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
lazy val dispatchV = "0.11.3"
lazy val dispatch = "net.databinder.dispatch" %% "dispatch-core" % dispatchV

lazy val root = (project in file(".")).
  enablePlugins(ScalaxbPlugin).
  settings(inThisBuild(List(
    organization  := "com.example",
    scalaVersion  := "2.11.8"
  ))).
  settings(
    name          := "foo-project",
    libraryDependencies ++= Seq(dispatch),
    libraryDependencies ++= {
      if (scalaVersion.value startsWith "2.10") Seq()
      else Seq(scalaXml, scalaParser)
    }).
  settings(
    scalaxbDispatchVersion in (Compile, scalaxb) := dispatchV,
    scalaxbPackageName in (Compile, scalaxb)     := "generated"
    // scalaxbPackageNames in (Compile, scalaxb)    := Map(uri("http://schemas.microsoft.com/2003/10/Serialization/") -> "microsoft.serialization"),
    // logLevel in (Compile, scalaxb) := Level.Debug
  )

coverageEnabled := false

