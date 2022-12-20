organization := "karedo"
name := "persist"
version := "0.0.2-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.mavenLocal

libraryDependencies ++= {
  val akkaV = "2.4.11"
  Seq(

//    "com.github.salat" %% "salat" % "1.10.0"
  "com.github.salat" %% "salat" % "CR-1.10.1-SNAPSHOT"

//    , "org.mongodb.scala" %% "mongo-scala-driver" % "1.2.1"
    //, "org.scalaz" %% "scalaz-core" % "7.2.6" // for Disjunction(Either class)
    , "com.typesafe" % "config" % "1.3.1"

    , "com.typesafe.akka" %% "akka-stream" % akkaV
    , "com.typesafe.akka" %% "akka-http-experimental" % akkaV
    , "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV
    , "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2"
    , "com.typesafe.akka" %% "akka-slf4j" % akkaV
    , "com.typesafe.akka"   %%  "akka-actor"        % akkaV

    , "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test"
    , "com.typesafe.akka"   %%  "akka-testkit"      % akkaV   % "test"

    //, "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV
    , "org.specs2" %% "specs2-core" % "3.8.5" % "test"
    , "org.specs2" %% "specs2-junit" % "3.8.5.1" % "test"
    , "junit" % "junit" % "4.8.1" % "test"
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

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

parallelExecution in Test := false

coverageEnabled := false
