name := "model"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.newzly"             %% "phantom-dsl"           % "0.3.0",
  "com.newzly"             %% "phantom-finagle"       % "0.3.0"
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
