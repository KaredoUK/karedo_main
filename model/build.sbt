organization := "parallelai.wallet"

name := "model"

version := "1.0"

scalaVersion := "2.10.2"

val phantomVersion = "0.5.0"

libraryDependencies ++= Seq(
  "com.newzly"             %% "phantom-dsl"           % phantomVersion,
  "com.newzly"             %% "phantom-finagle"       % phantomVersion,
  "org.scala-lang.modules" %% "scala-async"           % "0.9.0"
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
