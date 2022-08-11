name := "AkkaHttp"

version := "1.0"

scalaVersion := "2.11.8" //

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion = "2.4.10" // akka 2.4 is supporting akkahttp

  Seq(
    /*"com.typesafe.akka" %% "akka-actor" % akkaV, */
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,


    "org.scalactic" %% "scalactic" % "3.0.0",

    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test",
    "org.specs2" %% "specs2-core" % "3.8.4" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.3" % "test" // very simple http client
  )
}
    