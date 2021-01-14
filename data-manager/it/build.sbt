import sbt._

import sbtassembly.Plugin._
import AssemblyKeys._

Revolver.settings


name := """it"""

Common.settings

test in assembly := {}

version := "1.1"

resolvers ++= Common.sprayResolvers 

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"       % Common.akkaVersion,
  "com.typesafe.akka"  %% "akka-slf4j"       % Common.akkaVersion,
  "ch.qos.logback"      % "logback-classic"  % "1.0.13",
  "io.spray"           %% "spray-can"        % Common.sprayVersion,
  "io.spray"           %% "spray-routing"    % Common.sprayVersion,
  "commons-lang"        % "commons-lang"     % "2.3",
  "commons-io"          % "commons-io"       % "2.3",
  "commons-codec"       % "commons-codec"    % "1.9",
  Common.sprayClient,
  Common.sprayJson,
  Common.scalaAsync,
  Common.specs2 % "test",
  "io.spray"           %% "spray-testkit"    % Common.sprayVersion    % "test",
  "com.typesafe.akka"  %% "akka-testkit"     % Common.akkaVersion     % "test",
  "com.novocode"        % "junit-interface"  % "0.7"                  % "test->default",
//  "parallelai.wallet" %% "model" % "1.0" changing(),
  "com.github.tomakehurst" % "wiremock" % "1.38" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  Common.spraySwagger
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

mainClass in assembly := Some("Testing")


testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")





