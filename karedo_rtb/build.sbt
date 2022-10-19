organization := "karedo"

name := "rtb"

version := "0.0.2-SNAPSHOT"

scalaVersion := "2.11.8"

coverageEnabled := true

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += "spray repo" at "http://repo.spray.io"
resolvers += Resolver.mavenLocal
//resolvers += Resolver.typesafeRepo("releases")
//resolvers += Resolver.typesafeIvyRepo("releases")
resolvers += Resolver.sbtPluginRepo("releases")


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



scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.4.11"
  val sprayV = "1.3.2" //1.3.4 is latest but does not have build for spray-json
  val karedoV = "0.0.2-SNAPSHOT"
  Seq(
    "karedo" %% "persist" % karedoV,

//    "io.spray"            %%  "spray-can"         % sprayV,
//    "io.spray"            %%  "spray-json"        % sprayV,
    "io.spray"            %%  "spray-client"      % sprayV,
    "io.spray"            %%  "spray-http"        % sprayV,
    "io.spray"            %%  "spray-httpx"       % sprayV,
//    "io.spray"            %%  "spray-routing"     % sprayV,
//    "io.spray"            %%  "spray-testkit"     % sprayV  % "test",

    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",
    "com.typesafe.akka"   %%  "akka-actor"        % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"      % akkaV   % "test"

  )
}

mainClass in (run) := Some("karedo.rtb.runner.BidRunner")
