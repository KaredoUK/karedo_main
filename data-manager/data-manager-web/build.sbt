name := """data-manager-web"""

Common.settings

libraryDependencies ++= Seq(
  //filters,   // A set of built-in filters
  javaCore,  // The core Java API
  // WebJars pull in client-side web libraries
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "2.3.1",
  Common.sprayClient,
  Common.subcutExt
  // Add your own project dependencies in the form:
  // "group" % "artifact" % "version"
)


resolvers ++= Common.sprayResolvers

resolvers += Common.conjarResolver

play.Project.playScalaSettings
