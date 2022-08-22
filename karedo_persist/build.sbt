organization := "karedo"
name := "persist"
version := "0.0.1"

scalaVersion := "2.11.8"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.mavenLocal

libraryDependencies ++= {
  val akkaV = "2.4.11"
  Seq(

    "com.github.salat" %% "salat" % "1.10.0"
    , "com.typesafe" % "config" % "1.3.1"

    //, "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV
    , "org.specs2" %% "specs2-core" % "3.8.5" % "test"
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

