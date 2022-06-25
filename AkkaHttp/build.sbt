name := "AkkaHttp"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.3" // akka 2.4 is supporting akkahttp
  val scalaTestV  = "3.0.0"
  Seq(
    /*"com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV,*/

    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "org.specs2" %% "specs2-core" % "3.8.4" % "test",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.3" % "test" // very simple http client
  )
}
    