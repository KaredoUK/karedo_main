name := """data-manager-data-types"""

Common.settings

resolvers ++= Common.sprayResolvers

libraryDependencies ++= Seq(
  Common.sprayJson
  //,Common.spraySwagger
) ++ Common.jodaTime

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)

