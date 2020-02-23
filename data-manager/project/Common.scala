import sbt._
import Keys._

object Common {

  val settings: Seq[Setting[_]] = Seq (
    version := "1.0",
    scalaVersion := "2.10.2",
    organization := "com.parallelai"
  )

  val akkaVersion = "2.2.3"

  val sl4jVersion = "1.7.5"

  val sprayVersion = "1.2-20130712"
  val sprayJsonVersion = "1.2.3"

  val sprayResolvers = Seq (
    "spray repo" at "http://repo.spray.io",
    "spray nightlies" at "http://nightlies.spray.io"
  )

  val specs2 = "org.specs2" %% "specs2" % "1.14"
  val sprayJson = "io.spray" %% "spray-json" % sprayJsonVersion
}