//organization := "karedo"
//name := "routes"
//version := "0.0.2-SNAPSHOT"
//
//scalaVersion := "2.11.8"
//
//// jacoco.settings
//
//resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
//resolvers += Resolver.mavenLocal


libraryDependencies ++= {
  val akkaV = "2.4.11"
  val karedoV = "0.0.2-SNAPSHOT"
  val zxingV = "3.3.0"
  Seq(
    "karedo" %% "persist" % karedoV,
    "karedo" %% "rtb" % karedoV,

    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,


//    "org.slf4j" % "slf4j-api" % "1.7.5",
//    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "ch.qos.logback" % "logback-classic" % "1.1.10",
    "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test",

    "org.clapper" %% "classutil" % "1.0.11",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.specs2" %% "specs2-core" % "3.8.5" % "test",
    "org.specs2" %% "specs2-junit" % "3.8.5.1" % "test",
    "junit" % "junit" % "4.8.1" % "test",
    "com.google.zxing" % "core" % zxingV,
    "com.google.zxing" % "javase" % zxingV,
    "com.github.kenglxn.qrgen" % "javase" % "2.2.0",
    "io.igl" %% "jwt" % "1.2.0"
  )
}

// Setting up Twirl
//lazy val root = (project in file(".")).enablePlugins(SbtTwirl)
SbtTwirl.projectSettings
//sourceDirectories in (Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value

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
//parallelExecution in jacoco.Config := false

coverageEnabled := false
test in assembly := {}


