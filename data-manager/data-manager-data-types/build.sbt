name := """data-manager-data-types"""

Common.settings

version := "1.0"

resolvers ++= Common.sprayResolvers 

libraryDependencies ++= Seq(
  Common.sprayJson
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)
