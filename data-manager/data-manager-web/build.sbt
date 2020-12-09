name := """data-manager-web"""

Common.settings

libraryDependencies ++= Seq(
  //filters,   // A set of built-in filters
  // WebJars pull in client-side web libraries
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  Common.sprayClient,
  Common.subcutExt,
  Common.scalaAsync,
  "org.mongodb" %% "casbah" % "2.7.3"
)


resolvers ++= Common.sprayResolvers

resolvers += Common.conjarResolver

//resolvers ++= "casbah" at "https://oss.sonatype.org/content/repositories/snapshots/"
