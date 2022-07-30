// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.4")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

// to handle processes (e.g. mongodb embedded and Rest services)
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/whysoserious/sbt-process-runner/"))(
  Resolver.ivyStylePatterns)

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"


addSbtPlugin("io.scalac" % "sbt-process-runner" % "0.8.1")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

