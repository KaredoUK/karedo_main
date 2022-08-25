organization := "karedo"
name := "routes"
version := "0.0.2-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.mavenLocal

libraryDependencies ++= {
  val akkaV = "2.4.11"
  val karedoV = "0.0.2-SNAPSHOT"
  Seq(
    "karedo" %% "persist" % karedoV,
    "karedo" %% "rtb" % karedoV,

    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,


    "org.slf4j" % "slf4j-api" % "1.7.5",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",


    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.specs2" %% "specs2-core" % "3.8.5" % "test"
  )
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-Yrangepos"
)

parallelExecution in Test := false

test in assembly := {}


