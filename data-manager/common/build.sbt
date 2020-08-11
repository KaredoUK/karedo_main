name := "common"

Common.settings

resolvers += Common.conjarResolver

libraryDependencies ++= Seq(
  Common.subcutExt,
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
