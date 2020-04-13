organization := "parallelai.wallet"

name := "model"

version := "1.0"

scalaVersion := "2.10.2"

val phantomVersion = "0.5.0"

libraryDependencies ++= Seq(
  "com.newzly"              %% "phantom-dsl"            % phantomVersion,
  "org.scala-lang.modules"  %% "scala-async"            % "0.9.0",
  "org.cassandraunit"       %   "cassandra-unit"        % "1.1.1.2" % "test",
  "org.scalatest"           %% "scalatest"              % "2.1.5" % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)
