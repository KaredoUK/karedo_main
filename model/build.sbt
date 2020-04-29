organization := "parallelai.wallet"

name := "model"

version := "1.0"

scalaVersion := "2.10.2"

val phantomVersion = "0.5.0"

libraryDependencies ++= Seq(
  "com.newzly"              %% "phantom-dsl"            % phantomVersion,
  "org.scala-lang.modules"  %% "scala-async"            % "0.9.0",
  "com.novus"               %% "salat" % "1.9.8",
  "org.mongodb"             %% "casbah"                 % "2.7.1",
  "com.pragmasoft"          % "subcut_ext"               % "2.0",
  "parallelai.wallet"       %% "common"                  % "1.0",
  "com.github.athieriot"    %% "specs2-embedmongo"      % "0.6.0" % "test"
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
