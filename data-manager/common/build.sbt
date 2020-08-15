name := "common"

Common.settings

resolvers += Common.conjarResolver

libraryDependencies ++= Seq(
  Common.subcutExt,
  "com.typesafe"    	% "config"      		% "1.2.1",
  "io.spray"           %% "spray-httpx"        % Common.sprayVersion,
  "com.typesafe.akka"  %% "akka-actor"       % Common.akkaVersion,
  Common.sprayJson
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
