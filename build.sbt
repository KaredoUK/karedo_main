/** Project */
name := "wallet"

version := "0.1"

organization := "parallelai"

scalaVersion := "2.10.2"

crossScalaVersions := Seq("2.10.0", "2.10.1")


/** Compilation */
javacOptions ++= Seq()

javaOptions += "-Xmx1G"

scalacOptions ++= Seq("-deprecation", "-unchecked")

maxErrors := 20

pollInterval := 1000

logBuffered := false

cancelable := true
/*
testOptions := Seq(Tests.Filter(s =>
  Seq("Spec", "Suite", "Unit", "all").exists(s.endsWith(_)) &&
    !s.endsWith("FeaturesSpec") ||
    s.contains("UserGuide") ||
    s.contains("index") ||
    s.matches("org.specs2.guide.*")))

*/
