import sbt._

import sbtassembly.Plugin._
import AssemblyKeys._

import io.scalac.sbt.processrunner.{ProcessRunnerPlugin, ProcessInfo}
import ProcessRunnerPlugin.ProcessRunner
import ProcessRunnerPlugin.Keys.processInfoList

Revolver.settings


name := """data-manager-api"""

Common.settings

test in assembly := {}

version := "1.3.1"

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

//libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

// assembly will produce Karedo.jar
// this can be launched using java -Dconfig.resource=dummy.deployment.conf -jar data-manager-api\target\scala-2.11\Karedo.jar
// or you can use -Dconfig.file=your configuration file
mainClass in assembly := Some("Rest")

//jarName in assembly := "Karedo.jar"

//lazy val python = taskKey[Int]("Launches python tests")


//python := {
//  println("Testing python pwd: "+System.getProperty("user.dir"))
//  val ret:Int=Process("python3 -m py.test curltest --junitxml=Specs.xml") !;
//  println("Exit code is "+ret)
//  ret
//}
// important to use ~= so that any other initializations aren't dropped
// the _ discards the meaningless () value previously assigned to 'initialize'
fork := true
javaOptions := Seq("-Dconfig.resource=dummy.deployment.conf")





testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

ProcessRunnerPlugin.processRunnerSettings ++ Seq(
  scalaVersion := "2.11.4",
  scalacOptions := Seq("-deprecation", "-feature", "-encoding", "utf8", "-language:postfixOps"),
  organization := "io.scalac",
  // Register ProcessInfo objects
  processInfoList in ProcessRunner := Seq(Build.restapi)
)



