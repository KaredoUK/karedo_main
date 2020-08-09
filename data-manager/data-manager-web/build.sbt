name := """data-manager-web"""

Common.settings

libraryDependencies ++= Seq(
  //filters,   // A set of built-in filters
  javaCore,  // The core Java API
  // WebJars pull in client-side web libraries
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "2.3.1",
  Common.sprayClient,
  Common.subcutExt,
  Common.walletCommon,
  "parallelai.wallet" %% "model" % "1.0" changing(),
  Common.scalaAsync,
  "org.mongodb" %% "casbah" % "2.7.3"
)


resolvers ++= Common.sprayResolvers

resolvers += Common.conjarResolver

//resolvers ++= "casbah" at "https://oss.sonatype.org/content/repositories/snapshots/"

play.Project.playScalaSettings
