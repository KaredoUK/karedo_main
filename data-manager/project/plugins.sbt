// Comment to get more information during initialization
//logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.4")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

// to handle processes (e.g. mongodb embedded and Rest services)
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/whysoserious/sbt-process-runner/"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("io.scalac" % "sbt-process-runner" % "0.8.1")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

resolvers += "spray repo" at "http://repo.spray.io"

addSbtPlugin("io.spray" % "sbt-twirl" % "0.7.0")