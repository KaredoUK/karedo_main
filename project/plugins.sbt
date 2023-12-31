logLevel := Level.Warn

addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.5.1")

//addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.3.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.1")
//addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
//addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.1.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.3.4")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.3")

// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.8")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.6")

// This plugin adds forked run capabilities to Play projects which is needed for Activator.
//addSbtPlugin("com.typesafe.play" % "sbt-fork-run-plugin" % "2.5.10")

// This plugin represents functionality that is to be added to sbt in the future
//addSbtPlugin("org.scala-sbt" % "sbt-core-next" % "0.1.1")

// Google App Engine
//addSbtPlugin("com.eed3si9n" % "sbt-appengine" % "0.6.2")

// The Lagom plugin
//addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.3.5")

// Lightbend Conductr
//addSbtPlugin("com.lightbend.conductr" % "sbt-conductr" % "2.3.5")

// Needed for importing the project into Eclipse
//addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.1.0")

addSbtPlugin("com.typesafe.sbt"  % "sbt-scalariform" % "1.3.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.5.1")
//addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release"     % "1.0.3")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"    % "1.1")

// for BinTray
//addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")

// Publish to AWS S3
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"
addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.16.0")

// Added for additional safe compile
//resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
//addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.2")
