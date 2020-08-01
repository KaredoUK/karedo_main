organization := "parallelai.wallet"

name := "common"

version := "1.0"

scalaVersion := "2.10.2"

 resolvers += "conjars" at "http://conjars.org/repo"

libraryDependencies ++= Seq(
  "com.pragmasoft"          % "subcut_ext"               % "2.0",
  "com.typesafe"    	% "config"      		% "1.2.1"
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
